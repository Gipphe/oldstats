package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * {@code Quest.getState(client)} (real RuneLite code, not ours) calls
 * {@code client.runScript(4029, questId)} and reads the result off
 * {@code client.getIntStack()[0]} (2=FINISHED, 1=NOT_STARTED, else=IN_PROGRESS).
 * Both are plain {@link Client} methods, so this stubs them together to drive
 * real per-quest completion detection rather than only testing baseline
 * suppression.
 */
public class QuestTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private ClientThread clientThread;
    private QuestTracker tracker;

    /** questId -> state code. Defaults to 1 (NOT_STARTED) for any quest not listed. */
    private final Map<Integer, Integer> questStateCodes = new HashMap<>();
    private int lastQueriedQuestId = -1;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        clientThread = mock(ClientThread.class);
        tracker = new QuestTracker(apiClient, client, clientThread);
        questStateCodes.clear();

        // Run the deferred client-thread callback synchronously so tests can assert immediately.
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(clientThread).invokeLater(any(Runnable.class));

        doAnswer(invocation -> {
            lastQueriedQuestId = (int) invocation.getArgument(1);
            return null;
        }).when(client).runScript(any(Object[].class));
        when(client.getIntStack()).thenAnswer(
            invocation -> new int[] { questStateCodes.getOrDefault(lastQueriedQuestId, 1) }
        );
        when(client.getVarpValue(VarPlayerID.QP)).thenReturn(42);
    }

    @Test
    public void firstCheckEstablishesABaselineWithoutEmittingEvenForAlreadyFinishedQuests() {
        questStateCodes.put(Quest.COOKS_ASSISTANT.getId(), 2); // FINISHED from the start
        tracker.checkQuests();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void emitsACompletionWhenASingleQuestTransitionsToFinished() {
        tracker.checkQuests(); // baseline: everything NOT_STARTED

        questStateCodes.put(Quest.COOKS_ASSISTANT.getId(), 2);
        tracker.checkQuests();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.Quest event = (StatEvent.Quest) captor.getValue();
        assertEquals("COOKS_ASSISTANT", event.questName);
        assertEquals("COMPLETED", event.state);
        assertEquals(Integer.valueOf(42), event.questPoints);
    }

    @Test
    public void doesNotEmitForOtherQuestsThatRemainNotStarted() {
        tracker.checkQuests();
        questStateCodes.put(Quest.COOKS_ASSISTANT.getId(), 2);
        tracker.checkQuests();
        // Only one enqueue call total, not one per quest.
        verify(apiClient, times(1)).enqueue(any());
    }

    @Test
    public void anInProgressQuestDoesNotCountAsFinished() {
        tracker.checkQuests();
        questStateCodes.put(Quest.COOKS_ASSISTANT.getId(), 0); // IN_PROGRESS
        tracker.checkQuests();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resetAllowsAQuestThatWasAlreadyFinishedBeforeResetToBeTreatedAsAFreshBaseline() {
        tracker.checkQuests();
        questStateCodes.put(Quest.COOKS_ASSISTANT.getId(), 2);
        tracker.checkQuests(); // emits once

        tracker.reset();
        tracker.checkQuests(); // re-baselines at FINISHED, no emission

        verify(apiClient, times(1)).enqueue(any());
    }
}
