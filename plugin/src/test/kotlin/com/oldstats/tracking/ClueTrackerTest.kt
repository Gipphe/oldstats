package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.ChatMessageType
import net.runelite.api.events.ChatMessage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ClueTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var tracker: ClueTracker

    @Before
    fun setUp() {
        apiClient = mock()
        tracker = ClueTracker(apiClient)
    }

    private fun message(text: String, type: ChatMessageType = ChatMessageType.GAMEMESSAGE): ChatMessage =
        ChatMessage(null, type, "", text, "", 0)

    @Test
    fun `parses tier and count from the completion message`() {
        tracker.onChatMessage(message("You have completed 25 medium Treasure Trails."))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.ClueCompleted
        assertEquals("Medium", event.tier)
        assertEquals(25, event.count)
    }

    @Test
    fun `handles the singular Treasure Trail wording for a count of one`() {
        tracker.onChatMessage(message("You have completed 1 elite Treasure Trail."))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.ClueCompleted
        assertEquals("Elite", event.tier)
        assertEquals(1, event.count)
    }

    @Test
    fun `ignores unrelated chat messages`() {
        tracker.onChatMessage(message("You have completed a different quest."))
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `ignores messages that are not game messages or spam`() {
        tracker.onChatMessage(message("You have completed 5 easy Treasure Trails.", ChatMessageType.PUBLICCHAT))
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `strips color tags before matching`() {
        tracker.onChatMessage(message("You have completed <col=ef1020>10</col> hard Treasure Trails."))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.ClueCompleted
        assertEquals("Hard", event.tier)
        assertEquals(10, event.count)
    }
}
