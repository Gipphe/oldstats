package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.events.ActorDeath
import net.runelite.client.events.PlayerLootReceived

/**
 * Player-vs-player kills come from the same core LootManager mechanism as
 * monster kills ([PlayerLootReceived] mirrors [net.runelite.client.events.NpcLootReceived]),
 * so it's just as reliable. Own deaths use [ActorDeath] filtered to the
 * local player; value lost on death isn't computed (kept items via Protect
 * Item, skull status, etc. make an accurate figure nontrivial), so only the
 * death itself is recorded.
 */
class PvpTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    fun onPlayerLootReceived(event: PlayerLootReceived) {
        val opponentName = event.player.name ?: "Unknown"
        apiClient.enqueue(StatEvent.PlayerKill(opponentName = opponentName))
    }

    fun onActorDeath(event: ActorDeath) {
        if (event.actor == client.localPlayer) {
            apiClient.enqueue(StatEvent.PlayerDeath(valueLost = null))
        }
    }
}
