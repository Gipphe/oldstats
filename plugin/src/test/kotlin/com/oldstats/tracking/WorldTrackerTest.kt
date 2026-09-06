package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.WorldType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.EnumSet

class WorldTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: WorldTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = WorldTracker(apiClient, client)
    }

    private fun stubWorld(world: Int, types: EnumSet<WorldType> = EnumSet.noneOf(WorldType::class.java)) {
        whenever(client.world).thenReturn(world)
        whenever(client.worldType).thenReturn(types)
    }

    @Test
    fun `emits on the very first world sighting, unlike baseline-suppressing trackers`() {
        stubWorld(420)
        tracker.checkWorld()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.WorldChange
        assertEquals(420, event.world)
    }

    @Test
    fun `does not re-emit when the world is unchanged`() {
        stubWorld(420)
        tracker.checkWorld()
        tracker.checkWorld()
        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }

    @Test
    fun `emits again when the world changes, including its types`() {
        stubWorld(420)
        tracker.checkWorld()
        stubWorld(421, EnumSet.of(WorldType.MEMBERS, WorldType.PVP))
        tracker.checkWorld()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient, org.mockito.kotlin.times(2)).enqueue(captor.capture())
        val second = captor.secondValue as StatEvent.WorldChange
        assertEquals(421, second.world)
        assertEquals(setOf("MEMBERS", "PVP"), second.worldTypes.toSet())
    }

    @Test
    fun `ignores world 0, the not-yet-connected sentinel`() {
        stubWorld(0)
        tracker.checkWorld()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `reset allows the same world to be re-emitted, e g after a fresh login`() {
        stubWorld(420)
        tracker.checkWorld()
        tracker.reset()
        tracker.checkWorld()
        verify(apiClient, org.mockito.kotlin.times(2)).enqueue(any())
    }
}
