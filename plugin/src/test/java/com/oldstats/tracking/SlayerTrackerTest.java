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
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * SlayerTracker resolves task names via Client.getDBRowsByValue /
 * Client.getDBTableField (DB table 113, field 10), the same lookup path
 * RuneLite's own Slayer plugin uses instead of chat parsing. Wilderness
 * tasks cross-reference DB table 116 via VarbitID.SLAYER_TARGET_BOSSID.
 */
public class SlayerTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private SlayerTracker tracker;

    private static final int KRAKENS = 1234;
    private static final int KRAKENS_ROW = 500;
    private static final int SPECTRES = 5678;
    private static final int SPECTRES_ROW = 600;

    private static final int WILDY_BOSS_ID = 42;
    private static final int WILDY_CROSSREF_ROW = 700;
    private static final int WILDY_TASK_ROW = 800;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new SlayerTracker(apiClient, client);

        when(client.getDBRowsByValue(113, 0, 0, KRAKENS)).thenReturn(List.of(KRAKENS_ROW));
        when(client.getDBTableField(KRAKENS_ROW, 10, 0)).thenReturn(new Object[] { "Cave krakens" });

        when(client.getDBRowsByValue(113, 0, 0, SPECTRES)).thenReturn(List.of(SPECTRES_ROW));
        when(client.getDBTableField(SPECTRES_ROW, 10, 0)).thenReturn(new Object[] { "Aberrant spectres" });

        when(client.getDBRowsByValue(116, 1, 0, WILDY_BOSS_ID)).thenReturn(List.of(WILDY_CROSSREF_ROW));
        when(client.getDBTableField(WILDY_CROSSREF_ROW, 4, 0)).thenReturn(new Object[] { WILDY_TASK_ROW });
        when(client.getDBTableField(WILDY_TASK_ROW, 10, 0)).thenReturn(new Object[] { "Revenants" });
    }

    private void setTask(int creature, int amount) {
        setTask(creature, amount, 0, null);
    }

    private void setTask(int creature, int amount, int points) {
        setTask(creature, amount, points, null);
    }

    private void setTask(int creature, int amount, int points, Integer bossId) {
        when(client.getVarpValue(VarPlayerID.SLAYER_TARGET)).thenReturn(creature);
        when(client.getVarpValue(VarPlayerID.SLAYER_COUNT)).thenReturn(amount);
        when(client.getVarbitValue(VarbitID.SLAYER_POINTS)).thenReturn(points);
        if (bossId != null) {
            when(client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID)).thenReturn(bossId);
        }
    }

    @Test
    public void firstCheckEstablishesABaselineWithoutEmitting() {
        setTask(KRAKENS, 130);
        tracker.checkTask();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void emitsACompletionWithTheResolvedTaskNameAndPointsEarnedWhenTheRemainingCountHitsZero() {
        setTask(KRAKENS, 130, 100);
        tracker.checkTask(); // baseline

        setTask(KRAKENS, 0, 112);
        tracker.checkTask();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.SlayerTask event = (StatEvent.SlayerTask) captor.getValue();
        assertEquals("Cave krakens", event.taskName);
        assertEquals(Integer.valueOf(130), event.amountAssigned);
        assertEquals(Integer.valueOf(12), event.points);
    }

    @Test
    public void doesNotReEmitOnSubsequentTicksWhileTheFinishedTaskIsStillAssigned() {
        setTask(KRAKENS, 130, 100);
        tracker.checkTask(); // baseline

        setTask(KRAKENS, 0, 112);
        tracker.checkTask(); // emits once

        tracker.checkTask(); // still creature=KRAKENS, amount=0 -- must not double count
        tracker.checkTask();

        verify(apiClient, times(1)).enqueue(any());
    }

    @Test
    public void doesNotDoubleEmitWhenTheNextTaskIsAssignedAfterTheCountAlreadyHitZero() {
        setTask(KRAKENS, 130, 100);
        tracker.checkTask(); // baseline

        setTask(KRAKENS, 0, 112);
        tracker.checkTask(); // emits once for krakens

        setTask(SPECTRES, 90, 112); // new task assigned
        tracker.checkTask(); // must NOT re-emit the already-reported krakens completion

        verify(apiClient, times(1)).enqueue(any());
    }

    @Test
    public void cancellingOrSkippingATaskBeforeItsCountReachesZeroDoesNotEmitACompletion() {
        setTask(KRAKENS, 130, 100);
        tracker.checkTask(); // baseline

        setTask(SPECTRES, 90, 100); // switched away with 130 kills still remaining
        tracker.checkTask();

        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void emitsViaTheTaskChangedFallbackWhenTheCountWasAlreadyZeroAtBaseline() {
        // Plugin starts up (or resets) right as the previous task was already finished,
        // so the >0 -to- 0 transition was never observed directly.
        setTask(KRAKENS, 0, 100);
        tracker.checkTask(); // baseline, no emission even though remaining is already 0

        setTask(SPECTRES, 90, 112);
        tracker.checkTask();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.SlayerTask event = (StatEvent.SlayerTask) captor.getValue();
        assertEquals("Cave krakens", event.taskName);
    }

    @Test
    public void resolvesAWildernessTaskViaTheBossIdCrossReferenceInsteadOfTheSentinelCreatureId() {
        setTask(98, 40, 100, WILDY_BOSS_ID);
        tracker.checkTask(); // baseline

        setTask(98, 0, 130, WILDY_BOSS_ID);
        tracker.checkTask();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("Revenants", ((StatEvent.SlayerTask) captor.getValue()).taskName);
    }

    @Test
    public void aWildernessBossIdChangeWhileStillUnderTheSentinelCountsAsATaskChange() {
        setTask(98, 5, 100, WILDY_BOSS_ID);
        tracker.checkTask(); // baseline

        int otherBossId = 99;
        when(client.getDBRowsByValue(116, 1, 0, otherBossId)).thenReturn(List.of(701));
        when(client.getDBTableField(701, 4, 0)).thenReturn(new Object[] { 801 });
        when(client.getDBTableField(801, 10, 0)).thenReturn(new Object[] { "Vet'ion" });

        setTask(98, 3, 100, otherBossId); // switched wildy tasks with kills remaining
        tracker.checkTask();

        // Task changed with a nonzero remaining count on the old task: no completion.
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void noTaskAssignedDoesNotCrashOrEmit() {
        setTask(0, 0);
        tracker.checkTask(); // baseline
        setTask(0, 0);
        tracker.checkTask();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resetReEstablishesAFreshBaseline() {
        setTask(KRAKENS, 130, 100);
        tracker.checkTask();
        setTask(KRAKENS, 0, 112);
        tracker.checkTask(); // emits once

        tracker.reset();

        setTask(KRAKENS, 0, 112);
        tracker.checkTask(); // re-baselines at the already-finished state, no emission

        verify(apiClient, times(1)).enqueue(any());
    }
}
