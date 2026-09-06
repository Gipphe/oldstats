package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.client.events.NpcLootReceived
import net.runelite.client.game.ItemManager
import java.time.Duration
import java.time.Instant

/**
 * Tracks monster kills and the loot they drop via [NpcLootReceived], which
 * is posted by RuneLite's core LootManager service whenever a tracked NPC's
 * death is correlated with nearby item spawns — independent of whether the
 * built-in Loot Tracker plugin is enabled.
 */
class LootTracker(
    private val apiClient: OldStatsApiClient,
    private val itemManager: ItemManager,
) {
    private var lastBossKillName: String? = null
    private var lastBossKillAt: Instant? = null

    fun onNpcLootReceived(event: NpcLootReceived) {
        val npc = event.npc
        val npcName = npc.name ?: "Unknown"
        val isBoss = BossList.isBoss(npcName)

        apiClient.enqueue(StatEvent.Kill(npcName = npcName, npcId = npc.id, isBoss = isBoss))

        if (isBoss) {
            lastBossKillName = npcName
            lastBossKillAt = Instant.now()
        }

        for (item in event.items) {
            val composition = itemManager.getItemComposition(item.id)
            val price = itemManager.getItemPrice(item.id)
            apiClient.enqueue(
                StatEvent.Drop(
                    npcName = npcName,
                    itemName = composition.name,
                    itemId = item.id,
                    quantity = item.quantity,
                    value = price.toLong() * item.quantity,
                )
            )
        }
    }

    /** Used by [PersonalBestTracker] to attribute a "new personal best" chat message to a boss. */
    fun recentBossKill(within: Duration): String? {
        val name = lastBossKillName ?: return null
        val at = lastBossKillAt ?: return null
        return if (Duration.between(at, Instant.now()) <= within) name else null
    }
}
