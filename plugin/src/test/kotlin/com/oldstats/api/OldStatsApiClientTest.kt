package com.oldstats.api

import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

class OldStatsApiClientTest {
    private lateinit var httpClient: OkHttpClient
    private lateinit var call: Call
    private var serverUrl = "http://localhost:4000/"
    private var apiKey = "test-key"
    private lateinit var apiClient: OldStatsApiClient

    @Before
    fun setUp() {
        httpClient = mock()
        call = mock()
        whenever(httpClient.newCall(any())).thenReturn(call)
        apiClient = OldStatsApiClient(httpClient, Gson(), { serverUrl }, { apiKey })
    }

    private fun event(world: Int = 1) = StatEvent.WorldChange(world = world, worldTypes = emptyList())

    private fun response(successful: Boolean, code: Int = 200): Response =
        Response.Builder()
            .request(Request.Builder().url("http://localhost:4000/api/events").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(code)
            .message(if (successful) "OK" else "Error")
            .body(ResponseBody.create(null, "{}"))
            .build()

    /** Makes [callToStub]'s enqueue() synchronously invoke onResponse, like a same-thread fake dispatcher. */
    private fun stubSuccess(callToStub: Call, response: Response) {
        whenever(callToStub.enqueue(any())).thenAnswer { invocation ->
            invocation.getArgument<Callback>(0).onResponse(callToStub, response)
            null
        }
    }

    private fun stubFailure(callToStub: Call, exception: IOException) {
        whenever(callToStub.enqueue(any())).thenAnswer { invocation ->
            invocation.getArgument<Callback>(0).onFailure(callToStub, exception)
            null
        }
    }

    @Test
    fun `flushing an empty queue never touches the http client`() {
        apiClient.flush()
        verify(httpClient, never()).newCall(any())
    }

    @Test
    fun `does not send and keeps events queued when no api key is configured`() {
        apiKey = ""
        apiClient.enqueue(event())
        apiClient.flush()
        verify(httpClient, never()).newCall(any())

        apiKey = "now-configured"
        stubSuccess(call, response(successful = true))
        apiClient.flush()
        verify(httpClient, times(1)).newCall(any())
    }

    @Test
    fun `posts to api-events with a bearer token and trims a trailing slash from the server url`() {
        stubSuccess(call, response(successful = true))
        apiClient.enqueue(event())
        apiClient.flush()

        val captor = argumentCaptor<Request>()
        verify(httpClient).newCall(captor.capture())
        val request = captor.firstValue
        assertEquals("http://localhost:4000/api/events", request.url().toString())
        assertEquals("Bearer test-key", request.header("Authorization"))
    }

    @Test
    fun `batches every queued event into a single request`() {
        stubSuccess(call, response(successful = true))
        apiClient.enqueue(event(world = 1))
        apiClient.enqueue(event(world = 2))
        apiClient.enqueue(event(world = 3))
        apiClient.flush()

        verify(httpClient, times(1)).newCall(any())
    }

    @Test
    fun `requeues the batch for retry when the server rejects it`() {
        stubSuccess(call, response(successful = false, code = 500))
        apiClient.enqueue(event())
        apiClient.flush() // rejected, requeued

        val retryCall: Call = mock()
        whenever(httpClient.newCall(any())).thenReturn(retryCall)
        stubSuccess(retryCall, response(successful = true))
        apiClient.flush() // should resend the same requeued event

        verify(httpClient, times(2)).newCall(any())
    }

    @Test
    fun `requeues the batch for retry when sending throws`() {
        stubFailure(call, IOException("connection refused"))
        apiClient.enqueue(event())
        apiClient.flush() // swallows the exception, requeues

        val retryCall: Call = mock()
        whenever(httpClient.newCall(any())).thenReturn(retryCall)
        stubSuccess(retryCall, response(successful = true))
        apiClient.flush()

        verify(httpClient, times(2)).newCall(any())
    }

    @Test
    fun `flushing again with nothing newly queued does not send an empty request`() {
        stubSuccess(call, response(successful = true))
        apiClient.enqueue(event())
        apiClient.flush()
        apiClient.flush() // queue is empty now

        verify(httpClient, times(1)).newCall(any())
    }

    @Test
    fun `drops the oldest queued event once the queue exceeds its cap`() {
        stubSuccess(call, response(successful = true))
        // MAX_QUEUE_SIZE is 5000; fill past it so the very first event gets evicted.
        for (i in 0 until 5001) {
            apiClient.enqueue(event(world = i))
        }
        apiClient.flush()

        // MAX_BATCH_SIZE is 1000, so the 5000 remaining events go out as 5 batches.
        val captor = argumentCaptor<Request>()
        verify(httpClient, times(5)).newCall(captor.capture())
        val allBodies = captor.allValues.joinToString("\n") { bodyText(it) }
        assertTrue("evicted event (world 0) must not be present", !allBodies.contains("\"world\":0,"))
        assertTrue("most recent event (world 5000) must be present", allBodies.contains("\"world\":5000"))
    }

    @Test
    fun `caps each request at MAX_BATCH_SIZE events even when far more are queued`() {
        stubSuccess(call, response(successful = true))
        for (i in 0 until 2500) {
            apiClient.enqueue(event(world = i))
        }
        apiClient.flush()

        val captor = argumentCaptor<Request>()
        verify(httpClient, times(3)).newCall(captor.capture())
        val batchSizes = captor.allValues.map { bodyText(it).split("\"type\"").size - 1 }
        assertTrue("no batch should exceed 1000 events, got $batchSizes", batchSizes.all { it <= 1000 })
        assertEquals(2500, batchSizes.sum())
    }

    private fun bodyText(request: Request): String {
        val buffer = okio.Buffer()
        request.body()!!.writeTo(buffer)
        return buffer.readUtf8()
    }
}
