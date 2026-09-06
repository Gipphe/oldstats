package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.Player
import net.runelite.api.coords.WorldPoint
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
 * Drives the real Morytania mushroom patch (varbit 4771, region 13622) from
 * [FarmingWorldData], since [FarmingTracker] only reads patches belonging to
 * the region the player is physically standing in.
 */
class FarmingTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: FarmingTracker

    private val MUSHROOM_REGION = 13622
    private val MUSHROOM_VARBIT = 4771

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = FarmingTracker(apiClient, client)

        val player: Player = mock()
        whenever(player.worldLocation).thenReturn(WorldPoint.fromRegion(MUSHROOM_REGION, 0, 0, 0))
        whenever(client.localPlayer).thenReturn(player)
    }

    private fun setPatchValue(v: Int) {
        whenever(client.getVarbitValue(MUSHROOM_VARBIT)).thenReturn(v)
    }

    @Test
    fun `first check establishes a baseline without emitting`() {
        setPatchValue(0) // weeds
        tracker.checkPatches()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `does not touch patches outside the player's current region`() {
        val player: Player = mock()
        whenever(player.worldLocation).thenReturn(WorldPoint(0, 0, 0)) // region 0, no known patches
        whenever(client.localPlayer).thenReturn(player)

        tracker.checkPatches()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `planting a crop over weeds emits a PLANTED transition`() {
        setPatchValue(0) // WEEDS/GROWING
        tracker.checkPatches() // baseline

        setPatchValue(5) // MUSHROOM/GROWING
        tracker.checkPatches()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.FarmingPatch
        assertEquals("Morytania (Mushroom)", event.patchName)
        assertEquals("Mushroom", event.crop)
        assertEquals("PLANTED", event.state)
    }

    @Test
    fun `a fully grown crop emits a HARVESTABLE transition`() {
        setPatchValue(5) // MUSHROOM/GROWING
        tracker.checkPatches() // baseline

        setPatchValue(12) // MUSHROOM/HARVESTABLE
        tracker.checkPatches()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("HARVESTABLE", (captor.firstValue as StatEvent.FarmingPatch).state)
    }

    @Test
    fun `harvesting a ready crop back down to weeds emits a HARVESTED transition`() {
        setPatchValue(12) // MUSHROOM/HARVESTABLE
        tracker.checkPatches() // baseline

        setPatchValue(0) // WEEDS/GROWING
        tracker.checkPatches()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("HARVESTED", (captor.firstValue as StatEvent.FarmingPatch).state)
    }

    @Test
    fun `a diseased crop emits a DISEASED transition`() {
        setPatchValue(5) // MUSHROOM/GROWING
        tracker.checkPatches() // baseline

        setPatchValue(17) // MUSHROOM/DISEASED
        tracker.checkPatches()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("DISEASED", (captor.firstValue as StatEvent.FarmingPatch).state)
    }

    @Test
    fun `a dead crop emits a DEAD transition`() {
        setPatchValue(17) // MUSHROOM/DISEASED
        tracker.checkPatches() // baseline

        setPatchValue(22) // MUSHROOM/DEAD
        tracker.checkPatches()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("DEAD", (captor.firstValue as StatEvent.FarmingPatch).state)
    }

    @Test
    fun `does not re-emit when polled again with no actual state change`() {
        setPatchValue(5)
        tracker.checkPatches() // baseline

        setPatchValue(6) // still within the MUSHROOM+GROWING range -- same decoded state
        tracker.checkPatches()

        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `reset re-establishes a fresh baseline`() {
        setPatchValue(0)
        tracker.checkPatches()
        setPatchValue(5)
        tracker.checkPatches() // emits once

        tracker.reset()
        setPatchValue(5)
        tracker.checkPatches() // re-baselines, no emission

        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }
}
