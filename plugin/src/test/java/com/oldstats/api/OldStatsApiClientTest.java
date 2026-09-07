package com.oldstats.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OldStatsApiClientTest {
    private OkHttpClient httpClient;
    private Call call;
    private String serverUrl = "http://localhost:4000/";
    private String apiKey = "test-key";
    private OldStatsApiClient apiClient;

    @Before
    public void setUp() {
        httpClient = mock(OkHttpClient.class);
        call = mock(Call.class);
        when(httpClient.newCall(any())).thenReturn(call);
        apiClient = new OldStatsApiClient(httpClient, new Gson(), () -> serverUrl, () -> apiKey);
    }

    private StatEvent event() {
        return event(1);
    }

    private StatEvent event(int world) {
        return new StatEvent.WorldChange(world, Collections.emptyList());
    }

    private Response response(boolean successful) {
        return response(successful, successful ? 200 : 500);
    }

    private Response response(boolean successful, int code) {
        return new Response.Builder()
            .request(new Request.Builder().url("http://localhost:4000/api/events").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(successful ? "OK" : "Error")
            .body(ResponseBody.create(null, "{}"))
            .build();
    }

    /** Makes callToStub's enqueue() synchronously invoke onResponse, like a same-thread fake dispatcher. */
    private void stubSuccess(Call callToStub, Response response) {
        doAnswer(invocation -> {
            invocation.<Callback>getArgument(0).onResponse(callToStub, response);
            return null;
        }).when(callToStub).enqueue(any());
    }

    private void stubFailure(Call callToStub, IOException exception) {
        doAnswer(invocation -> {
            invocation.<Callback>getArgument(0).onFailure(callToStub, exception);
            return null;
        }).when(callToStub).enqueue(any());
    }

    @Test
    public void flushingAnEmptyQueueNeverTouchesTheHttpClient() {
        apiClient.flush();
        verify(httpClient, never()).newCall(any());
    }

    @Test
    public void doesNotSendAndKeepsEventsQueuedWhenNoApiKeyIsConfigured() {
        apiKey = "";
        apiClient.enqueue(event());
        apiClient.flush();
        verify(httpClient, never()).newCall(any());

        apiKey = "now-configured";
        stubSuccess(call, response(true));
        apiClient.flush();
        verify(httpClient, times(1)).newCall(any());
    }

    @Test
    public void postsToApiEventsWithABearerTokenAndTrimsATrailingSlashFromTheServerUrl() {
        stubSuccess(call, response(true));
        apiClient.enqueue(event());
        apiClient.flush();

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(httpClient).newCall(captor.capture());
        Request request = captor.getValue();
        assertEquals("http://localhost:4000/api/events", request.url().toString());
        assertEquals("Bearer test-key", request.header("Authorization"));
    }

    @Test
    public void batchesEveryQueuedEventIntoASingleRequest() {
        stubSuccess(call, response(true));
        apiClient.enqueue(event(1));
        apiClient.enqueue(event(2));
        apiClient.enqueue(event(3));
        apiClient.flush();

        verify(httpClient, times(1)).newCall(any());
    }

    @Test
    public void requeuesTheBatchForRetryWhenTheServerRejectsIt() {
        stubSuccess(call, response(false, 500));
        apiClient.enqueue(event());
        apiClient.flush(); // rejected, requeued

        Call retryCall = mock(Call.class);
        when(httpClient.newCall(any())).thenReturn(retryCall);
        stubSuccess(retryCall, response(true));
        apiClient.flush(); // should resend the same requeued event

        verify(httpClient, times(2)).newCall(any());
    }

    @Test
    public void requeuesTheBatchForRetryWhenSendingThrows() {
        stubFailure(call, new IOException("connection refused"));
        apiClient.enqueue(event());
        apiClient.flush(); // swallows the exception, requeues

        Call retryCall = mock(Call.class);
        when(httpClient.newCall(any())).thenReturn(retryCall);
        stubSuccess(retryCall, response(true));
        apiClient.flush();

        verify(httpClient, times(2)).newCall(any());
    }

    @Test
    public void flushingAgainWithNothingNewlyQueuedDoesNotSendAnEmptyRequest() {
        stubSuccess(call, response(true));
        apiClient.enqueue(event());
        apiClient.flush();
        apiClient.flush(); // queue is empty now

        verify(httpClient, times(1)).newCall(any());
    }

    @Test
    public void dropsTheOldestQueuedEventOnceTheQueueExceedsItsCap() {
        stubSuccess(call, response(true));
        // MAX_QUEUE_SIZE is 5000; fill past it so the very first event gets evicted.
        for (int i = 0; i < 5001; i++) {
            apiClient.enqueue(event(i));
        }
        apiClient.flush();

        // MAX_BATCH_SIZE is 1000, so the 5000 remaining events go out as 5 batches.
        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(httpClient, times(5)).newCall(captor.capture());
        StringBuilder allBodies = new StringBuilder();
        for (Request r : captor.getAllValues()) {
            allBodies.append(bodyText(r)).append('\n');
        }
        assertTrue("evicted event (world 0) must not be present", !allBodies.toString().contains("\"world\":0,"));
        assertTrue("most recent event (world 5000) must be present", allBodies.toString().contains("\"world\":5000"));
    }

    @Test
    public void capsEachRequestAtMaxBatchSizeEventsEvenWhenFarMoreAreQueued() {
        stubSuccess(call, response(true));
        for (int i = 0; i < 2500; i++) {
            apiClient.enqueue(event(i));
        }
        apiClient.flush();

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(httpClient, times(3)).newCall(captor.capture());
        List<Integer> batchSizes = new ArrayList<>();
        int total = 0;
        for (Request r : captor.getAllValues()) {
            int count = bodyText(r).split("\"type\"", -1).length - 1;
            batchSizes.add(count);
            total += count;
        }
        assertTrue("no batch should exceed 1000 events, got " + batchSizes, batchSizes.stream().allMatch(s -> s <= 1000));
        assertEquals(2500, total);
    }

    private String bodyText(Request request) {
        Buffer buffer = new Buffer();
        try {
            request.body().writeTo(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.readUtf8();
    }
}
