package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Skill
import net.runelite.api.events.StatChanged
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class XpTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var tracker: XpTracker

    @Before
    fun setUp() {
        apiClient = mock()
        tracker = XpTracker(apiClient)
    }

    @Test
    fun `first sighting of a skill establishes a baseline without emitting`() {
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1000, 50, 50))
        verifyNoMoreInteractions(apiClient)
    }

    @Test
    fun `xp increase after baseline emits an xp_gain with the correct delta`() {
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1000, 50, 50))
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1500, 50, 50))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.XpGain
        assertEquals("SLAYER", event.skill)
        assertEquals(1500L, event.xp)
        assertEquals(500L, event.xpGained)
        assertEquals(50, event.level)
    }

    @Test
    fun `level increase emits a level_up in addition to the xp_gain`() {
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1000, 50, 50))
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1500, 51, 51))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient, org.mockito.kotlin.times(2)).enqueue(captor.capture())
        assertTrue(captor.allValues.any { it is StatEvent.XpGain })
        val levelUp = captor.allValues.first { it is StatEvent.LevelUp } as StatEvent.LevelUp
        assertEquals("SLAYER", levelUp.skill)
        assertEquals(51, levelUp.level)
    }

    @Test
    fun `no change in xp or level emits nothing`() {
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1000, 50, 50))
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1000, 50, 50))
        verify(apiClient, org.mockito.kotlin.never()).enqueue(org.mockito.kotlin.any())
    }

    @Test
    fun `reset clears baselines so the next sighting is treated as a fresh login`() {
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1000, 50, 50))
        tracker.reset()
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1500, 50, 50))
        // Post-reset, this is a first sighting again — no emission expected.
        verifyNoMoreInteractions(apiClient)
    }

    @Test
    fun `independent skills track separate baselines`() {
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1000, 50, 50))
        tracker.onStatChanged(StatChanged(Skill.MAGIC, 2000, 60, 60))
        tracker.onStatChanged(StatChanged(Skill.SLAYER, 1100, 50, 50))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.XpGain
        assertEquals("SLAYER", event.skill)
        assertEquals(100L, event.xpGained)
    }
}
