package com.oldstats.api;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Buffers stat events in memory and periodically flushes them to the
 * OldStats server. The queue is in-process only: events are dropped if the
 * plugin is unloaded or the client crashes while the server is unreachable.
 */
public class OldStatsApiClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Logger log = LoggerFactory.getLogger(OldStatsApiClient.class);

    private static final int MAX_QUEUE_SIZE = 5000;

    // Must not exceed the server's `ingestBatch.events.max(...)` (server/src/types/events.ts) —
    // a batch bigger than that gets the whole request rejected with 400, not partially accepted.
    private static final int MAX_BATCH_SIZE = 1000;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Supplier<String> serverUrlProvider;
    private final Supplier<String> apiKeyProvider;

    private final ConcurrentLinkedQueue<StatEvent> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger queueSize = new AtomicInteger(0);

    public OldStatsApiClient(
        OkHttpClient httpClient,
        Gson gson,
        Supplier<String> serverUrlProvider,
        Supplier<String> apiKeyProvider
    ) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.serverUrlProvider = serverUrlProvider;
        this.apiKeyProvider = apiKeyProvider;
    }

    public void enqueue(StatEvent event) {
        if (queueSize.get() >= MAX_QUEUE_SIZE) {
            if (queue.poll() != null) {
                queueSize.decrementAndGet();
            }
        }
        queue.add(event);
        queueSize.incrementAndGet();
    }

    /**
     * Drains the queue and sends it in batches of at most {@link #MAX_BATCH_SIZE}.
     * Never blocks the calling thread — each batch is dispatched via OkHttp's
     * own threadpool ({@link Call#enqueue}), so this is also safe to call from
     * {@code shutDown()}/{@code startUp()}.
     *
     * <p>Bounded to a snapshot of the queue size taken up front, not "keep
     * going until empty": a failed batch gets requeued (see {@link #sendBatch}),
     * and looping on live queue state would retry it inline, immediately,
     * forever, if a failure ever completes synchronously within the same
     * call stack (never true for real OkHttp, whose callbacks always run on
     * its own dispatcher thread — but true of a naive synchronous test
     * double, and not a risk worth depending on OkHttp's async guarantee
     * for). Anything requeued this way is simply picked up by the next
     * scheduled flush() instead.
     */
    public void flush() {
        if (queue.isEmpty()) return;

        String apiKey = apiKeyProvider.get();
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("OldStats: no API key configured, skipping flush");
            return;
        }

        int remaining = queueSize.get();
        while (remaining > 0) {
            List<StatEvent> batch = new ArrayList<>(Math.min(remaining, MAX_BATCH_SIZE));
            while (batch.size() < MAX_BATCH_SIZE && batch.size() < remaining) {
                StatEvent event = queue.poll();
                if (event == null) break;
                queueSize.decrementAndGet();
                batch.add(event);
            }
            if (batch.isEmpty()) break;
            remaining -= batch.size();
            sendBatch(batch, apiKey);
        }
    }

    private void sendBatch(List<StatEvent> batch, String apiKey) {
        String serverUrl = serverUrlProvider.get();
        while (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        String body = gson.toJson(Collections.singletonMap("events", batch));
        Request request = new Request.Builder()
            .url(serverUrl + "/api/events")
            .header("Authorization", "Bearer " + apiKey)
            .post(RequestBody.create(JSON, body))
            .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("OldStats: failed to reach server, requeueing {} events: {}", batch.size(), e.getMessage());
                batch.forEach(OldStatsApiClient.this::enqueue);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful()) {
                        String responseBody;
                        try {
                            responseBody = response.body() != null ? response.body().string() : null;
                        } catch (IOException e) {
                            responseBody = null;
                        }
                        log.warn(
                            "OldStats: server rejected batch of {} events: {} {}",
                            batch.size(),
                            response.code(),
                            responseBody
                        );
                        batch.forEach(OldStatsApiClient.this::enqueue);
                    } else {
                        log.debug("OldStats: sent {} events", batch.size());
                    }
                }
            }
        });
    }
}
