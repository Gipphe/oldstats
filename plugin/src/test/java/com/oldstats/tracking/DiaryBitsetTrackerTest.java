package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import com.oldstats.tracking.diary.DiaryTaskData;
import com.oldstats.tracking.diary.DiaryTaskDef;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * DiaryBitsetTracker decodes the same per-area VarPlayer bitsets RuneLite's
 * Quest Helper plugin uses (ported into com.oldstats.tracking.diary.DiaryTaskData).
 * These tests drive real entries from that ported table — Ardougne Easy's
 * "Essence Mine" (bit 0), "Steal Cake" (bit 1) and "Sell Silk" (bit 2), all
 * on VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY — rather than fabricated data.
 */
public class DiaryBitsetTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private DiaryBitsetTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new DiaryBitsetTracker(apiClient, client);
    }

    private void setArdougneVarp(int value) {
        when(client.getVarpValue(VarPlayerID.ARDOUNGE_ACHIEVEMENT_DIARY)).thenReturn(value);
    }

    @Test
    public void unlikeOtherTrackersTheVeryFirstCheckReportsTheFullCurrentState() {
        setArdougneVarp(0b011); // bits 0 and 1 set: Essence Mine + Steal Cake done

        tracker.checkTasks();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(451)).enqueue(captor.capture());
        List<StatEvent.DiaryTaskProgress> ardougneEvents = captor.getAllValues().stream()
            .map(e -> (StatEvent.DiaryTaskProgress) e)
            .filter(e -> e.diaryArea.equals("ARDOUGNE") && e.tier.equals("EASY"))
            .collect(Collectors.toList());
        assertEquals(true, findByTaskName(ardougneEvents, "Essence Mine").completed);
        assertEquals(true, findByTaskName(ardougneEvents, "Steal Cake").completed);
        assertEquals(false, findByTaskName(ardougneEvents, "Sell Silk").completed);
    }

    private StatEvent.DiaryTaskProgress findByTaskName(List<StatEvent.DiaryTaskProgress> events, String taskName) {
        return events.stream().filter(e -> e.taskName.equals(taskName)).findFirst().orElseThrow();
    }

    @Test
    public void doesNotReEmitForTasksWhoseBitIsUnchangedBetweenChecks() {
        setArdougneVarp(0b001);
        tracker.checkTasks(); // 451 baseline events

        setArdougneVarp(0b001); // identical
        tracker.checkTasks();

        verify(apiClient, times(451)).enqueue(any());
    }

    @Test
    public void emitsOnlyForTheSpecificTaskWhoseBitActuallyFlipped() {
        setArdougneVarp(0b000);
        tracker.checkTasks(); // baseline: everything false, 451 events
        clearInvocations(apiClient);

        setArdougneVarp(0b100); // bit 2 set: Sell Silk done
        tracker.checkTasks();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(1)).enqueue(captor.capture());
        StatEvent.DiaryTaskProgress event = (StatEvent.DiaryTaskProgress) captor.getValue();
        assertEquals("Sell Silk", event.taskName);
        assertEquals(true, event.completed);
    }

    @Test
    public void aTaskCanFlipFromCompletedBackToNotCompletedAndIsReportedEitherWay() {
        setArdougneVarp(0b001); // Essence Mine done
        tracker.checkTasks();
        clearInvocations(apiClient);

        setArdougneVarp(0b000); // Essence Mine un-done (e.g. a bugged account state)
        tracker.checkTasks();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(1)).enqueue(captor.capture());
        StatEvent.DiaryTaskProgress event = (StatEvent.DiaryTaskProgress) captor.getValue();
        assertEquals("Essence Mine", event.taskName);
        assertEquals(false, event.completed);
    }

    @Test
    public void resetCausesTheNextCheckToReReportTheFullStateAgain() {
        setArdougneVarp(0b001);
        tracker.checkTasks(); // 451 baseline events

        tracker.reset();

        setArdougneVarp(0b001); // unchanged value, but state was cleared
        tracker.checkTasks();

        verify(apiClient, times(451 * 2)).enqueue(any());
    }

    @Test
    public void coversEveryAreaExceptKaramjasLegacyEasyMediumAndHardTiers() {
        Set<Map.Entry<String, String>> areas = new HashSet<>();
        for (DiaryTaskDef task : DiaryTaskData.ALL_TASKS) {
            areas.add(new AbstractMap.SimpleEntry<>(task.area, task.tier));
        }
        assertTrue(areas.contains(new AbstractMap.SimpleEntry<>("KARAMJA", "ELITE")));
        assertFalse(areas.contains(new AbstractMap.SimpleEntry<>("KARAMJA", "EASY")));
        assertFalse(areas.contains(new AbstractMap.SimpleEntry<>("KARAMJA", "MEDIUM")));
        assertFalse(areas.contains(new AbstractMap.SimpleEntry<>("KARAMJA", "HARD")));
        assertEquals(451, DiaryTaskData.ALL_TASKS.size());
    }
}
