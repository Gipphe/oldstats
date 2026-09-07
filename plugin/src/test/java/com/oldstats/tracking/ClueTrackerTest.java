package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ClueTrackerTest {
    private OldStatsApiClient apiClient;
    private ClueTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        tracker = new ClueTracker(apiClient);
    }

    private ChatMessage message(String text) {
        return message(text, ChatMessageType.GAMEMESSAGE);
    }

    private ChatMessage message(String text, ChatMessageType type) {
        return new ChatMessage(null, type, "", text, "", 0);
    }

    @Test
    public void parsesTierAndCountFromTheCompletionMessage() {
        tracker.onChatMessage(message("You have completed 25 medium Treasure Trails."));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.ClueCompleted event = (StatEvent.ClueCompleted) captor.getValue();
        assertEquals("Medium", event.tier);
        assertEquals(Integer.valueOf(25), event.count);
    }

    @Test
    public void handlesTheSingularTreasureTrailWordingForACountOfOne() {
        tracker.onChatMessage(message("You have completed 1 elite Treasure Trail."));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.ClueCompleted event = (StatEvent.ClueCompleted) captor.getValue();
        assertEquals("Elite", event.tier);
        assertEquals(Integer.valueOf(1), event.count);
    }

    @Test
    public void ignoresUnrelatedChatMessages() {
        tracker.onChatMessage(message("You have completed a different quest."));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void ignoresMessagesThatAreNotGameMessagesOrSpam() {
        tracker.onChatMessage(message("You have completed 5 easy Treasure Trails.", ChatMessageType.PUBLICCHAT));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void stripsColorTagsBeforeMatching() {
        tracker.onChatMessage(message("You have completed <col=ef1020>10</col> hard Treasure Trails."));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.ClueCompleted event = (StatEvent.ClueCompleted) captor.getValue();
        assertEquals("Hard", event.tier);
        assertEquals(Integer.valueOf(10), event.count);
    }
}
