package com.oldstats.api

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
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
    private val serverUrlProvider: () -> String,
    private val apiKeyProvider: () -> String,
) {
    private val gson = Gson()
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

    /** Drains the queue and sends everything in one batch. Safe to call from a background thread. */
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

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    log.warn("OldStats: server rejected batch of {} events: {}", batch.size, response.code())
                    // requeue for retry on next flush
                    batch.forEach { enqueue(it) }
                } else {
                    log.debug("OldStats: sent {} events", batch.size)
                }
            }
        } catch (e: Exception) {
            log.warn("OldStats: failed to reach server, requeueing {} events: {}", batch.size, e.message)
            batch.forEach { enqueue(it) }
        }
    }
}
