package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AchievementDiaryTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private AchievementDiaryTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new AchievementDiaryTracker(apiClient, client);
    }

    @Test
    public void emitsACompletedDiaryWhenABooleanTierVarbitFlipsTo1() {
        tracker.checkDiaries(); // baseline, everything reads 0

        when(client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE)).thenReturn(1);
        tracker.checkDiaries();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.DiaryCompleted event = (StatEvent.DiaryCompleted) captor.getValue();
        assertEquals("ARDOUGNE", event.diaryArea);
        assertEquals("ELITE", event.tier);
    }

    @Test
    public void karamjaEasyDiaryCompletesOnlyOnceTheTaskCountReachesItsWikiSourcedTotalOf10() {
        tracker.checkDiaries(); // baseline at 0

        when(client.getVarbitValue(VarbitID.KARAMJA_EASY_COUNT)).thenReturn(9);
        tracker.checkDiaries();
        verify(apiClient, never()).enqueue(any());

        when(client.getVarbitValue(VarbitID.KARAMJA_EASY_COUNT)).thenReturn(10);
        tracker.checkDiaries();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.DiaryCompleted event = (StatEvent.DiaryCompleted) captor.getValue();
        assertEquals("KARAMJA", event.diaryArea);
        assertEquals("EASY", event.tier);
    }

    @Test
    public void karamjaMediumDiaryRequiresACountOf19Not10() {
        tracker.checkDiaries();
        when(client.getVarbitValue(VarbitID.KARAMJA_MED_COUNT)).thenReturn(18);
        tracker.checkDiaries();
        verify(apiClient, never()).enqueue(any());

        when(client.getVarbitValue(VarbitID.KARAMJA_MED_COUNT)).thenReturn(19);
        tracker.checkDiaries();
        verify(apiClient).enqueue(any());
    }

    @Test
    public void doesNotReEmitForADiaryAlreadyCompleteAtBaseline() {
        when(client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE)).thenReturn(1);
        tracker.checkDiaries(); // baseline: already complete
        tracker.checkDiaries(); // unchanged
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resetReEstablishesAFreshBaseline() {
        tracker.checkDiaries();
        when(client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE)).thenReturn(1);
        tracker.checkDiaries(); // emits once

        tracker.reset();
        tracker.checkDiaries(); // re-baselines, no emission

        verify(apiClient, times(1)).enqueue(any());
    }
}
