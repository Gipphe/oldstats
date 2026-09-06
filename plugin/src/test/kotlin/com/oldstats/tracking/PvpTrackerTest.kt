package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Actor
import net.runelite.api.Client
import net.runelite.api.Player
import net.runelite.api.events.ActorDeath
import net.runelite.client.events.PlayerLootReceived
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PvpTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: PvpTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = PvpTracker(apiClient, client)
    }

    @Test
    fun `records the opponent name from a player kill`() {
        val opponent: Player = mock()
        whenever(opponent.name).thenReturn("Some Pker")

        tracker.onPlayerLootReceived(PlayerLootReceived(opponent, emptyList()))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("Some Pker", (captor.firstValue as StatEvent.PlayerKill).opponentName)
    }

    @Test
    fun `falls back to Unknown when the opponent has no name`() {
        val opponent: Player = mock()
        whenever(opponent.name).thenReturn(null)

        tracker.onPlayerLootReceived(PlayerLootReceived(opponent, emptyList()))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("Unknown", (captor.firstValue as StatEvent.PlayerKill).opponentName)
    }

    @Test
    fun `records a death when the local player dies`() {
        val localPlayer: Player = mock()
        whenever(client.localPlayer).thenReturn(localPlayer)

        tracker.onActorDeath(ActorDeath(localPlayer))

        verify(apiClient).enqueue(any<StatEvent.PlayerDeath>())
    }

    @Test
    fun `ignores another actor dying`() {
        val localPlayer: Player = mock()
        val someNpc: Actor = mock()
        whenever(client.localPlayer).thenReturn(localPlayer)

        tracker.onActorDeath(ActorDeath(someNpc))

        verify(apiClient, never()).enqueue(any())
    }
}
