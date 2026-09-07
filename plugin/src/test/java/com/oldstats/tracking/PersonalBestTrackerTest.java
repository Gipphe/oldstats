package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.Collections;
import net.runelite.api.ChatMessageType;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PersonalBestTrackerTest {
    private OldStatsApiClient apiClient;
    private LootTracker lootTracker;
    private PersonalBestTracker tracker;

    @Before
    public void setUp() {
        lootTracker = new LootTracker(mock(OldStatsApiClient.class), mock(ItemManager.class));
        apiClient = mock(OldStatsApiClient.class);
        tracker = new PersonalBestTracker(apiClient, lootTracker);
    }

    private ChatMessage message(String text) {
        return message(text, ChatMessageType.GAMEMESSAGE);
    }

    private ChatMessage message(String text, ChatMessageType type) {
        return new ChatMessage(null, type, "", text, "", 0);
    }

    private void recordBossKill(String name) {
        NPC npc = mock(NPC.class);
        when(npc.getName()).thenReturn(name);
        lootTracker.onNpcLootReceived(new NpcLootReceived(npc, Collections.emptyList()));
    }

    @Test
    public void attributesANewPersonalBestToTheMostRecentlyKilledBoss() {
        recordBossKill("Vorkath");
        tracker.onChatMessage(message("Fight duration: <col=ff0000>1:15.40</col> (new personal best)"));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.PersonalBest event = (StatEvent.PersonalBest) captor.getValue();
        assertEquals("Vorkath", event.activityName);
        assertEquals(75.4, event.durationSeconds, 0.001);
    }

    @Test
    public void parsesHourMinuteSecondDurationsCorrectly() {
        recordBossKill("Great Olm");
        tracker.onChatMessage(message("Challenge duration: <col=ff0000>1:23:45.60</col> (new personal best)"));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.PersonalBest event = (StatEvent.PersonalBest) captor.getValue();
        assertEquals(1 * 3600.0 + 23 * 60.0 + 45.60, event.durationSeconds, 0.001);
    }

    @Test
    public void doesNotEmitWhenThereIsNoRecentBossKillToAttributeItTo() {
        tracker.onChatMessage(message("Fight duration: <col=ff0000>1:15.40</col> (new personal best)"));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void ignoresAFightDurationMessageThatIsNotANewPersonalBest() {
        recordBossKill("Vorkath");
        tracker.onChatMessage(message("Fight duration: <col=ff0000>1:15.40</col>. Personal best: 1:10.00"));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void theNegativeLookbehindExcludesTotalCompletionTimeMessages() {
        recordBossKill("Vorkath");
        tracker.onChatMessage(message("Total completion time: <col=ff0000>1:15.40</col> (new personal best)"));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void ignoresMessagesOfTheWrongChatType() {
        recordBossKill("Vorkath");
        tracker.onChatMessage(
            message("Fight duration: <col=ff0000>1:15.40</col> (new personal best)", ChatMessageType.PUBLICCHAT)
        );
        verify(apiClient, never()).enqueue(any());
    }
}
