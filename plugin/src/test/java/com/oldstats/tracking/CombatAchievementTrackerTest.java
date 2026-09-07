package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

/**
 * {@link CombatAchievementTracker} reflects over the real {@link VarbitID}
 * class rather than an injectable fake, so these tests drive real known
 * fields ({@code CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED} etc). Mockito's
 * default answer for an unstubbed int-returning method is 0, which
 * conveniently doubles as "not completed" for the ~399 other task varbits
 * these tests don't touch.
 */
public class CombatAchievementTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private CombatAchievementTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new CombatAchievementTracker(apiClient, client);
    }

    @Test
    public void firstCheckEstablishesABaselineWithoutEmitting() {
        when(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1);
        tracker.checkTasks();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void emitsACombatAchievementWithADerivedNameWhenATaskFlipsToCompleted() {
        tracker.checkTasks(); // baseline: everything reads 0

        when(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1);
        when(client.getVarbitValue(VarbitID.CA_POINTS)).thenReturn(155);
        tracker.checkTasks();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.CombatAchievement event = (StatEvent.CombatAchievement) captor.getValue();
        assertEquals("Vorkath Killcount 1", event.taskName);
        assertEquals(Integer.valueOf(155), event.totalPoints);
    }

    @Test
    public void derivesAReadableNameFromADifferentTasksVarbitConstant() {
        tracker.checkTasks();
        when(client.getVarbitValue(VarbitID.CA_TASK_JAD_SPEED_2_COMPLETED)).thenReturn(1);
        tracker.checkTasks();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("Jad Speed 2", ((StatEvent.CombatAchievement) captor.getValue()).taskName);
    }

    @Test
    public void reportsNullTotalPointsWhenCaPointsReadsZero() {
        tracker.checkTasks();
        when(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1);
        tracker.checkTasks();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertNull(((StatEvent.CombatAchievement) captor.getValue()).totalPoints);
    }

    @Test
    public void doesNotReEmitForATaskThatWasAlreadyCompletedAtBaseline() {
        when(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1);
        tracker.checkTasks(); // baseline already-completed
        tracker.checkTasks(); // still completed, no change
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resetReEstablishesAFreshBaseline() {
        tracker.checkTasks();
        when(client.getVarbitValue(VarbitID.CA_TASK_VORKATH_KILLCOUNT_1_COMPLETED)).thenReturn(1);
        tracker.checkTasks(); // emits once
        tracker.reset();
        tracker.checkTasks(); // re-baselines at the now-completed state, no emission

        verify(apiClient, times(1)).enqueue(any());
    }
}
