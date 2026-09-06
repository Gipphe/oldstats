package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.Quest
import net.runelite.api.QuestState
import net.runelite.api.VarPlayer
import java.util.concurrent.ConcurrentHashMap

/**
 * There is no dedicated "quest completed" event in the RuneLite API — quest
 * progress lives in varbits/varps with no single change notification, so
 * this re-checks every [Quest]'s state on each VarbitChanged tick (cheap,
 * ~200 enum reads) and reports newly-FINISHED quests. The first check after
 * (re)login only establishes a baseline so already-completed quests aren't
 * reported again.
 */
class QuestTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private val lastState = ConcurrentHashMap<Quest, QuestState>()

    fun reset() {
        lastState.clear()
    }

    fun checkQuests() {
        for (quest in Quest.values()) {
            val state = quest.getState(client)
            val previous = lastState.put(quest, state)
            if (previous != null && previous != QuestState.FINISHED && state == QuestState.FINISHED) {
                apiClient.enqueue(
                    StatEvent.Quest(
                        questName = quest.name,
                        state = "COMPLETED",
                        questPoints = client.getVarpValue(VarPlayer.QUEST_POINTS),
                    )
                )
            }
        }
    }
}
