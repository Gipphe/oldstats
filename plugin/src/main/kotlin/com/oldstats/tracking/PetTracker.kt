package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.ChatMessageType
import net.runelite.api.events.ChatMessage
import net.runelite.api.events.GameTick
import net.runelite.api.events.NpcSpawned

/**
 * There's no dedicated "pet received" event, and the chat message doesn't
 * name the pet. This watches for one of the three known pet-drop messages,
 * then attributes it to whichever NPC spawns next to the player within a
 * few ticks (pets that auto-follow spawn this way). Pets received directly
 * to the inventory instead (follower slot already occupied, or no space)
 * won't have a name resolved and are reported as "Unknown pet".
 */
class PetTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private var pendingSince: Int? = null
    private var tick = 0

    companion object {
        private const val PET_WAIT_TICKS = 3
        private val PET_MESSAGES = listOf(
            "you have a funny feeling like you're being followed",
            "you feel something weird sneaking into your backpack",
        )
    }

    fun onChatMessage(event: ChatMessage) {
        if (event.type != ChatMessageType.GAMEMESSAGE && event.type != ChatMessageType.SPAM) return
        val message = event.message.lowercase()
        if (PET_MESSAGES.any { message.contains(it) }) {
            pendingSince = tick
        }
    }

    fun onNpcSpawned(event: NpcSpawned) {
        val since = pendingSince ?: return
        if (tick - since > PET_WAIT_TICKS) {
            pendingSince = null
            apiClient.enqueue(StatEvent.PetReceived(petName = null))
            return
        }
        val npc = event.npc
        val playerLoc = client.localPlayer?.worldLocation ?: return
        val npcLoc = npc.worldLocation ?: return
        if (npcLoc.distanceTo(playerLoc) <= 1) {
            pendingSince = null
            apiClient.enqueue(StatEvent.PetReceived(petName = npc.name))
        }
    }

    fun onGameTick(@Suppress("UNUSED_PARAMETER") event: GameTick) {
        tick++
        val since = pendingSince ?: return
        if (tick - since > PET_WAIT_TICKS) {
            pendingSince = null
            apiClient.enqueue(StatEvent.PetReceived(petName = null))
        }
    }
}
