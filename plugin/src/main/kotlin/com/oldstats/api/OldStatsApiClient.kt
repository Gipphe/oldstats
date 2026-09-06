package com.oldstats.api

import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

private val JSON = MediaType.parse("application/json; charset=utf-8")
private val log = LoggerFactory.getLogger(OldStatsApiClient::class.java)

/**
 * Buffers stat events in memory and periodically flushes them to the
 * OldStats server. The queue is in-process only: events are dropped if the
 * plugin is unloaded or the client crashes while the server is unreachable.
 */
class OldStatsApiClient(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val serverUrlProvider: () -> String,
    private val apiKeyProvider: () -> String,
) {
    private val queue = ConcurrentLinkedQueue<StatEvent>()
    private val queueSize = AtomicInteger(0)

    companion object {
        private const val MAX_QUEUE_SIZE = 5000

        // Must not exceed the server's `ingestBatch.events.max(...)` (server/src/types/events.ts) —
        // a batch bigger than that gets the whole request rejected with 400, not partially accepted.
        private const val MAX_BATCH_SIZE = 1000
    }

    fun enqueue(event: StatEvent) {
        if (queueSize.get() >= MAX_QUEUE_SIZE) {
            queue.poll()?.let { queueSize.decrementAndGet() }
        }
        queue.add(event)
        queueSize.incrementAndGet()
    }

    /**
     * Drains the queue and sends it in batches of at most [MAX_BATCH_SIZE].
     * Never blocks the calling thread — each batch is dispatched via OkHttp's
     * own threadpool ([Call.enqueue]), so this is also safe to call from
     * `shutDown()`/`startUp()`.
     *
     * Bounded to a snapshot of the queue size taken up front, not "keep
     * going until empty": a failed batch gets requeued (see [sendBatch]),
     * and looping on live queue state would retry it inline, immediately,
     * forever, if a failure ever completes synchronously within the same
     * call stack (never true for real OkHttp, whose callbacks always run on
     * its own dispatcher thread — but true of a naive synchronous test
     * double, and not a risk worth depending on OkHttp's async guarantee
     * for). Anything requeued this way is simply picked up by the next
     * scheduled flush() instead.
     */
    fun flush() {
        if (queue.isEmpty()) return

        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            log.debug("OldStats: no API key configured, skipping flush")
            return
        }

        var remaining = queueSize.get()
        while (remaining > 0) {
            val batch = ArrayList<StatEvent>(minOf(remaining, MAX_BATCH_SIZE))
            while (batch.size < MAX_BATCH_SIZE && batch.size < remaining) {
                val event = queue.poll() ?: break
                queueSize.decrementAndGet()
                batch.add(event)
            }
            if (batch.isEmpty()) break
            remaining -= batch.size
            sendBatch(batch, apiKey)
        }
    }

    private fun sendBatch(batch: List<StatEvent>, apiKey: String) {
        val serverUrl = serverUrlProvider().trimEnd('/')
        val body = gson.toJson(mapOf("events" to batch))
        val request = Request.Builder()
            .url("$serverUrl/api/events")
            .header("Authorization", "Bearer $apiKey")
            .post(RequestBody.create(JSON, body))
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                log.warn("OldStats: failed to reach server, requeueing {} events: {}", batch.size, e.message)
                batch.forEach { enqueue(it) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        log.warn(
                            "OldStats: server rejected batch of {} events: {} {}",
                            batch.size,
                            it.code(),
                            it.body()?.string(),
                        )
                        batch.forEach { event -> enqueue(event) }
                    } else {
                        log.debug("OldStats: sent {} events", batch.size)
                    }
                }
            }
        })
    }
}
