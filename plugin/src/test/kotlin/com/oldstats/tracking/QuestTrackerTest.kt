package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.Quest
import net.runelite.api.gameval.VarPlayerID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * `Quest.getState(client)` (real RuneLite code, not ours) calls
 * `client.runScript(4029, questId)` and reads the result off
 * `client.getIntStack()[0]` (2=FINISHED, 1=NOT_STARTED, else=IN_PROGRESS).
 * Both are plain [Client] methods, so this stubs them together to drive
 * real per-quest completion detection rather than only testing baseline
 * suppression.
 */
class QuestTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: QuestTracker

    /** questId -> state code. Defaults to 1 (NOT_STARTED) for any quest not listed. */
    private val questStateCodes = mutableMapOf<Int, Int>()
    private var lastQueriedQuestId = -1

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = QuestTracker(apiClient, client)
        questStateCodes.clear()

        whenever(client.runScript(anyVararg())).thenAnswer { invocation ->
            lastQueriedQuestId = invocation.arguments[1] as Int
            null
        }
        whenever(client.intStack).thenAnswer { intArrayOf(questStateCodes[lastQueriedQuestId] ?: 1) }
        whenever(client.getVarpValue(VarPlayerID.QP)).thenReturn(42)
    }

    @Test
    fun `first check establishes a baseline without emitting, even for already-finished quests`() {
        questStateCodes[Quest.COOKS_ASSISTANT.id] = 2 // FINISHED from the start
        tracker.checkQuests()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `emits a completion when a single quest transitions to FINISHED`() {
        tracker.checkQuests() // baseline: everything NOT_STARTED

        questStateCodes[Quest.COOKS_ASSISTANT.id] = 2
        tracker.checkQuests()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.Quest
        assertEquals("COOKS_ASSISTANT", event.questName)
        assertEquals("COMPLETED", event.state)
        assertEquals(42, event.questPoints)
    }

    @Test
    fun `does not emit for other quests that remain not started`() {
        tracker.checkQuests()
        questStateCodes[Quest.COOKS_ASSISTANT.id] = 2
        tracker.checkQuests()
        // Only one enqueue call total, not one per quest.
        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }

    @Test
    fun `an in-progress quest does not count as finished`() {
        tracker.checkQuests()
        questStateCodes[Quest.COOKS_ASSISTANT.id] = 0 // IN_PROGRESS
        tracker.checkQuests()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `reset allows a quest that was already finished before reset to be treated as a fresh baseline`() {
        tracker.checkQuests()
        questStateCodes[Quest.COOKS_ASSISTANT.id] = 2
        tracker.checkQuests() // emits once

        tracker.reset()
        tracker.checkQuests() // re-baselines at FINISHED, no emission

        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }
}
