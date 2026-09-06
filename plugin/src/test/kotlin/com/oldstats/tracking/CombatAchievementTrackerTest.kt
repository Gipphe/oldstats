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

/**
 * [CombatAchievementTracker] reflects over the real [VarbitID] class rather
 * than an injectable fake, so these tests drive real known fields
 * (`CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED` etc). Mockito's default answer
 * for an unstubbed int-returning method is 0, which conveniently doubles as
 * "not completed" for the ~399 other task varbits these tests don't touch.
 */
class CombatAchievementTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: CombatAchievementTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = CombatAchievementTracker(apiClient, client)
    }

    @Test
    fun `first check establishes a baseline without emitting`() {
        whenever(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1)
        tracker.checkTasks()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `emits a combat achievement with a derived name when a task flips to completed`() {
        tracker.checkTasks() // baseline: everything reads 0

        whenever(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1)
        whenever(client.getVarbitValue(VarbitID.CA_POINTS)).thenReturn(155)
        tracker.checkTasks()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.CombatAchievement
        assertEquals("Vorkath Killcount 1", event.taskName)
        assertEquals(155, event.totalPoints)
    }

    @Test
    fun `derives a readable name from a different task's varbit constant`() {
        tracker.checkTasks()
        whenever(client.getVarbitValue(VarbitID.CA_TASK_JAD_SPEED_2_COMPLETED)).thenReturn(1)
        tracker.checkTasks()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("Jad Speed 2", (captor.firstValue as StatEvent.CombatAchievement).taskName)
    }

    @Test
    fun `reports null total points when CA_POINTS reads zero`() {
        tracker.checkTasks()
        whenever(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1)
        tracker.checkTasks()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals(null, (captor.firstValue as StatEvent.CombatAchievement).totalPoints)
    }

    @Test
    fun `does not re-emit for a task that was already completed at baseline`() {
        whenever(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1)
        tracker.checkTasks() // baseline already-completed
        tracker.checkTasks() // still completed, no change
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `reset re-establishes a fresh baseline`() {
        tracker.checkTasks()
        whenever(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1)
        tracker.checkTasks() // emits once
        tracker.reset()
        tracker.checkTasks() // re-baselines at the now-completed state, no emission

        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }
}
