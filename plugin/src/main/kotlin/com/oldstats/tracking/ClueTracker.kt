package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.ChatMessageType
import net.runelite.api.events.ChatMessage
import net.runelite.client.util.Text

/**
 * Tracks clue scroll completions via the completion chat message, using the
 * same regex RuneLite's own Loot Tracker plugin matches against
 * (`CLUE_SCROLL_PATTERN` in `LootTrackerPlugin`), extended with our own
 * capture group for the running total since the original only captures the
 * tier.
 */
class ClueTracker(private val apiClient: OldStatsApiClient) {

    companion object {
        private val CLUE_PATTERN = Regex("""You have completed (\d+) ([a-z]+) Treasure Trails?\.""")
    }

    fun onChatMessage(event: ChatMessage) {
        if (event.type != ChatMessageType.GAMEMESSAGE && event.type != ChatMessageType.SPAM) return
        val message = Text.removeTags(event.message)

        CLUE_PATTERN.find(message)?.let { match ->
            val count = match.groupValues[1].toIntOrNull()
            val tier = match.groupValues[2]
            apiClient.enqueue(StatEvent.ClueCompleted(tier = tier.replaceFirstChar(Char::uppercase), count = count))
        }
    }
}
