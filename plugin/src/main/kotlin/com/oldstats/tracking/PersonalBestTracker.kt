package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.ChatMessageType
import net.runelite.api.events.ChatMessage
import java.time.Duration

/**
 * Tracks new boss personal bests using the same regex RuneLite's own
 * `ChatCommandsPlugin` matches (`NEW_PB_PATTERN`) against the
 * "Fight duration: ... (new personal best)"-style message — which requires
 * the player to have the "Fight duration" chat setting enabled in-game, same
 * as the real plugin. That message never names the boss, so this attributes
 * it to whichever boss [LootTracker] most recently confirmed a kill for;
 * activities without a corresponding NPC kill (raids, agility laps, etc.)
 * aren't attributed and are skipped.
 */
class PersonalBestTracker(
    private val apiClient: OldStatsApiClient,
    private val lootTracker: LootTracker,
) {
    companion object {
        private val NEW_PB_PATTERN = Regex(
            """(?i)(?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?<!total )completion time:) (?:<col=[0-9a-f]{6}>|@.+?@)([0-9:]+(?:\.[0-9]+)?)</col> \(new personal best\)"""
        )
        private val ATTRIBUTION_WINDOW: Duration = Duration.ofSeconds(10)
    }

    fun onChatMessage(event: ChatMessage) {
        if (event.type != ChatMessageType.GAMEMESSAGE && event.type != ChatMessageType.SPAM) return

        val match = NEW_PB_PATTERN.find(event.message) ?: return
        val seconds = parseDuration(match.groupValues[1]) ?: return
        val boss = lootTracker.recentBossKill(ATTRIBUTION_WINDOW) ?: return

        apiClient.enqueue(StatEvent.PersonalBest(activityName = boss, durationSeconds = seconds))
    }

    private fun parseDuration(text: String): Double? {
        val parts = text.split(":").map { it.toDoubleOrNull() ?: return null }
        return parts.foldIndexed(0.0) { index, acc, part ->
            val placeFromRight = parts.size - 1 - index
            acc + part * Math.pow(60.0, placeFromRight.toDouble())
        }
    }
}
