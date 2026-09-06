package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client

/**
 * Emits an event whenever the current world changes (including the very
 * first sighting each session — unlike the baseline-suppression pattern
 * used elsewhere, "I'm on this world starting now" is itself useful data
 * for the server's time-per-world breakdown, which derives duration from
 * the gap between consecutive events).
 */
class WorldTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private var lastWorld: Int? = null

    fun reset() {
        lastWorld = null
    }

    fun checkWorld() {
        val world = client.world
        if (world == 0 || world == lastWorld) return
        lastWorld = world
        val types = client.worldType.map { it.name }
        apiClient.enqueue(StatEvent.WorldChange(world = world, worldTypes = types))
    }
}
