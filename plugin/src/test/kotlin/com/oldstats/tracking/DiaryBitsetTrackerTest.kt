package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.gameval.VarPlayerID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * [DiaryBitsetTracker] decodes the same per-area VarPlayer bitsets RuneLite's
 * Quest Helper plugin uses (ported into [com.oldstats.tracking.diary.DiaryTaskData]).
 * These tests drive real entries from that ported table — Ardougne Easy's
 * "Essence Mine" (bit 0), "Steal Cake" (bit 1) and "Sell Silk" (bit 2), all
 * on [VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY] — rather than fabricated data.
 */
class DiaryBitsetTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: DiaryBitsetTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = DiaryBitsetTracker(apiClient, client)
    }

    private fun setArdougneVarp(value: Int) {
        whenever(client.getVarpValue(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY)).thenReturn(value)
    }

    @Test
    fun `unlike other trackers, the very first check reports the full current state`() {
        setArdougneVarp(0b011) // bits 0 and 1 set: Essence Mine + Steal Cake done

        tracker.checkTasks()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient, times(451)).enqueue(captor.capture())
        val ardougneEvents = captor.allValues.map { it as StatEvent.DiaryTaskProgress }.filter { it.diaryArea == "ARDOUGNE" && it.tier == "EASY" }
        assertEquals(true, ardougneEvents.first { it.taskName == "Essence Mine" }.completed)
        assertEquals(true, ardougneEvents.first { it.taskName == "Steal Cake" }.completed)
        assertEquals(false, ardougneEvents.first { it.taskName == "Sell Silk" }.completed)
    }

    @Test
    fun `does not re-emit for tasks whose bit is unchanged between checks`() {
        setArdougneVarp(0b001)
        tracker.checkTasks() // 451 baseline events

        setArdougneVarp(0b001) // identical
        tracker.checkTasks()

        verify(apiClient, times(451)).enqueue(any())
    }

    @Test
    fun `emits only for the specific task whose bit actually flipped`() {
        setArdougneVarp(0b000)
        tracker.checkTasks() // baseline: everything false, 451 events
        org.mockito.kotlin.clearInvocations(apiClient)

        setArdougneVarp(0b100) // bit 2 set: Sell Silk done
        tracker.checkTasks()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient, times(1)).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.DiaryTaskProgress
        assertEquals("Sell Silk", event.taskName)
        assertEquals(true, event.completed)
    }

    @Test
    fun `a task can flip from completed back to not completed and is reported either way`() {
        setArdougneVarp(0b001) // Essence Mine done
        tracker.checkTasks()
        org.mockito.kotlin.clearInvocations(apiClient)

        setArdougneVarp(0b000) // Essence Mine un-done (e.g. a bugged account state)
        tracker.checkTasks()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient, times(1)).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.DiaryTaskProgress
        assertEquals("Essence Mine", event.taskName)
        assertEquals(false, event.completed)
    }

    @Test
    fun `reset causes the next check to re-report the full state again`() {
        setArdougneVarp(0b001)
        tracker.checkTasks() // 451 baseline events

        tracker.reset()

        setArdougneVarp(0b001) // unchanged value, but state was cleared
        tracker.checkTasks()

        verify(apiClient, times(451 * 2)).enqueue(any())
    }

    @Test
    fun `covers every area except karamja's legacy easy medium and hard tiers`() {
        val areas = com.oldstats.tracking.diary.DiaryTaskData.ALL_TASKS.map { it.area to it.tier }.toSet()
        assertTrue(areas.contains("KARAMJA" to "ELITE"))
        assertTrue(!areas.contains("KARAMJA" to "EASY"))
        assertTrue(!areas.contains("KARAMJA" to "MEDIUM"))
        assertTrue(!areas.contains("KARAMJA" to "HARD"))
        assertEquals(451, com.oldstats.tracking.diary.DiaryTaskData.ALL_TASKS.size)
    }
}
