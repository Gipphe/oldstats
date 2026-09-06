package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.ChatMessageType
import net.runelite.api.Client
import net.runelite.api.NPC
import net.runelite.api.Player
import net.runelite.api.coords.WorldPoint
import net.runelite.api.events.ChatMessage
import net.runelite.api.events.GameTick
import net.runelite.api.events.NpcSpawned
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PetTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: PetTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = PetTracker(apiClient, client)

        val localPlayer: Player = mock()
        whenever(localPlayer.worldLocation).thenReturn(WorldPoint(100, 100, 0))
        whenever(client.localPlayer).thenReturn(localPlayer)
    }

    private fun chatMessage(text: String): ChatMessage = ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", text, "", 0)

    private fun npcAt(name: String, point: WorldPoint): NPC {
        val npc: NPC = mock()
        whenever(npc.name).thenReturn(name)
        whenever(npc.worldLocation).thenReturn(point)
        return npc
    }

    @Test
    fun `attributes the pet to an npc spawning right next to the player`() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you're being followed."))
        tracker.onNpcSpawned(NpcSpawned(npcAt("Vorki", WorldPoint(100, 101, 0))))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("Vorki", (captor.firstValue as StatEvent.PetReceived).petName)
    }

    @Test
    fun `recognizes the alternate 'sneaking into your backpack' message`() {
        tracker.onChatMessage(chatMessage("You feel something weird sneaking into your backpack."))
        tracker.onNpcSpawned(npcAt("Baby chinchompa", WorldPoint(100, 100, 0)).let { NpcSpawned(it) })

        verify(apiClient).enqueue(any<StatEvent.PetReceived>())
    }

    @Test
    fun `does not treat the near-miss 'would have been followed' message as a pet`() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you would have been followed..."))
        tracker.onNpcSpawned(NpcSpawned(npcAt("Random npc", WorldPoint(100, 100, 0))))

        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `ignores an npc spawning far away from the player`() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you're being followed."))
        tracker.onNpcSpawned(NpcSpawned(npcAt("Random npc", WorldPoint(500, 500, 0))))

        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `times out to an unknown pet after too many ticks pass with no matching spawn`() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you're being followed."))
        repeat(4) { tracker.onGameTick(GameTick()) }

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals(null, (captor.firstValue as StatEvent.PetReceived).petName)
    }

    @Test
    fun `an npc spawn with no preceding pet message is ignored`() {
        tracker.onNpcSpawned(NpcSpawned(npcAt("Random npc", WorldPoint(100, 100, 0))))
        verify(apiClient, never()).enqueue(any())
    }
}
