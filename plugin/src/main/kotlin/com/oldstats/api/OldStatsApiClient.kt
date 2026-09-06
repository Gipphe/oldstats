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
    }

    fun enqueue(event: StatEvent) {
        if (queueSize.get() >= MAX_QUEUE_SIZE) {
            queue.poll()?.let { queueSize.decrementAndGet() }
        }
        queue.add(event)
        queueSize.incrementAndGet()
    }

    /**
     * Drains the queue and sends everything in one batch. Never blocks the calling
     * thread — the request is dispatched via OkHttp's own threadpool ([Call.enqueue]),
     * so this is also safe to call from `shutDown()`/`startUp()`.
     */
    fun flush() {
        if (queue.isEmpty()) return

        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            log.debug("OldStats: no API key configured, skipping flush")
            return
        }

        val batch = ArrayList<StatEvent>(queueSize.get())
        while (true) {
            val event = queue.poll() ?: break
            queueSize.decrementAndGet()
            batch.add(event)
        }
        if (batch.isEmpty()) return

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
                        log.warn("OldStats: server rejected batch of {} events: {}", batch.size, it.code())
                        batch.forEach { event -> enqueue(event) }
                    } else {
                        log.debug("OldStats: sent {} events", batch.size)
                    }
                }
            }
        })
    }
}
