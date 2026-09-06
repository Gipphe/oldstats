package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.Quest
import net.runelite.api.QuestState
import net.runelite.api.gameval.VarPlayerID
import net.runelite.client.callback.ClientThread
import java.util.concurrent.ConcurrentHashMap

/**
 * There is no dedicated "quest completed" event in the RuneLite API — quest
 * progress lives in varbits/varps with no single change notification, so
 * this re-checks every [Quest]'s state on each VarbitChanged tick and
 * reports newly-FINISHED quests. The first check after (re)login only
 * establishes a baseline so already-completed quests aren't reported again.
 *
 * [Quest.getState] internally calls `client.runScript(ScriptID.QUEST_STATUS_GET, ...)`
 * — a real CS2 script invocation, not a cheap field read. Varbits are
 * frequently changed by scripts themselves, so VarbitChanged can fire while
 * one is still executing; calling another script synchronously from there
 * throws `AssertionError: scripts are not reentrant`. Deferring to the next
 * client tick via [clientThread] guarantees a clean call stack.
 */
class QuestTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
    private val clientThread: ClientThread,
) {
    private val lastState = ConcurrentHashMap<Quest, QuestState>()

    fun reset() {
        lastState.clear()
    }

    fun checkQuests() {
        clientThread.invokeLater(Runnable {
            for (quest in Quest.values()) {
                val state = quest.getState(client)
                val previous = lastState.put(quest, state)
                if (previous != null && previous != QuestState.FINISHED && state == QuestState.FINISHED) {
                    apiClient.enqueue(
                        StatEvent.Quest(
                            questName = quest.name,
                            state = "COMPLETED",
                            questPoints = client.getVarpValue(VarPlayerID.QP),
                        )
                    )
                }
            }
        })
    }
}
