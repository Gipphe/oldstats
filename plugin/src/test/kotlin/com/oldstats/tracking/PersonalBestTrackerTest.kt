package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.ChatMessageType
import net.runelite.api.NPC
import net.runelite.api.events.ChatMessage
import net.runelite.client.events.NpcLootReceived
import net.runelite.client.game.ItemManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PersonalBestTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var lootTracker: LootTracker
    private lateinit var tracker: PersonalBestTracker

    @Before
    fun setUp() {
        apiClient = mock()
        lootTracker = LootTracker(mock(), mock<ItemManager>())
        tracker = PersonalBestTracker(apiClient, lootTracker)
    }

    private fun message(text: String, type: ChatMessageType = ChatMessageType.GAMEMESSAGE): ChatMessage =
        ChatMessage(null, type, "", text, "", 0)

    private fun recordBossKill(name: String) {
        val npc: NPC = mock()
        whenever(npc.name).thenReturn(name)
        lootTracker.onNpcLootReceived(NpcLootReceived(npc, emptyList()))
    }

    @Test
    fun `attributes a new personal best to the most recently killed boss`() {
        recordBossKill("Vorkath")
        tracker.onChatMessage(message("Fight duration: <col=ff0000>1:15.40</col> (new personal best)"))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.PersonalBest
        assertEquals("Vorkath", event.activityName)
        assertEquals(75.4, event.durationSeconds, 0.001)
    }

    @Test
    fun `parses hour-minute-second durations correctly`() {
        recordBossKill("Great Olm")
        tracker.onChatMessage(message("Challenge duration: <col=ff0000>1:23:45.60</col> (new personal best)"))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.PersonalBest
        assertEquals(1 * 3600.0 + 23 * 60.0 + 45.60, event.durationSeconds, 0.001)
    }

    @Test
    fun `does not emit when there is no recent boss kill to attribute it to`() {
        tracker.onChatMessage(message("Fight duration: <col=ff0000>1:15.40</col> (new personal best)"))
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `ignores a fight duration message that is not a new personal best`() {
        recordBossKill("Vorkath")
        tracker.onChatMessage(message("Fight duration: <col=ff0000>1:15.40</col>. Personal best: 1:10.00"))
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `the negative lookbehind excludes 'total completion time' messages`() {
        recordBossKill("Vorkath")
        tracker.onChatMessage(message("Total completion time: <col=ff0000>1:15.40</col> (new personal best)"))
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `ignores messages of the wrong chat type`() {
        recordBossKill("Vorkath")
        tracker.onChatMessage(message("Fight duration: <col=ff0000>1:15.40</col> (new personal best)", ChatMessageType.PUBLICCHAT))
        verify(apiClient, never()).enqueue(any())
    }
}
