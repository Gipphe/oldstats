package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.gameval.VarbitID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AchievementDiaryTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: AchievementDiaryTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = AchievementDiaryTracker(apiClient, client)
    }

    @Test
    fun `emits a completed diary when a boolean tier varbit flips to 1`() {
        tracker.checkDiaries() // baseline, everything reads 0

        whenever(client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE)).thenReturn(1)
        tracker.checkDiaries()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.DiaryCompleted
        assertEquals("ARDOUGNE", event.diaryArea)
        assertEquals("ELITE", event.tier)
    }

    @Test
    fun `karamja easy diary completes only once the task count reaches its wiki-sourced total of 10`() {
        tracker.checkDiaries() // baseline at 0

        whenever(client.getVarbitValue(VarbitID.KARAMJA_EASY_COUNT)).thenReturn(9)
        tracker.checkDiaries()
        verify(apiClient, never()).enqueue(any())

        whenever(client.getVarbitValue(VarbitID.KARAMJA_EASY_COUNT)).thenReturn(10)
        tracker.checkDiaries()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.DiaryCompleted
        assertEquals("KARAMJA", event.diaryArea)
        assertEquals("EASY", event.tier)
    }

    @Test
    fun `karamja medium diary requires a count of 19, not 10`() {
        tracker.checkDiaries()
        whenever(client.getVarbitValue(VarbitID.KARAMJA_MED_COUNT)).thenReturn(18)
        tracker.checkDiaries()
        verify(apiClient, never()).enqueue(any())

        whenever(client.getVarbitValue(VarbitID.KARAMJA_MED_COUNT)).thenReturn(19)
        tracker.checkDiaries()
        verify(apiClient).enqueue(any())
    }

    @Test
    fun `does not re-emit for a diary already complete at baseline`() {
        whenever(client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE)).thenReturn(1)
        tracker.checkDiaries() // baseline: already complete
        tracker.checkDiaries() // unchanged
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `reset re-establishes a fresh baseline`() {
        tracker.checkDiaries()
        whenever(client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE)).thenReturn(1)
        tracker.checkDiaries() // emits once

        tracker.reset()
        tracker.checkDiaries() // re-baselines, no emission

        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }
}
