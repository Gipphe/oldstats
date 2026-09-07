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
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Drives the real Morytania mushroom patch (varbit 4771, region 13622) from
 * FarmingWorldData, since FarmingTracker only reads patches belonging to
 * the region the player is physically standing in.
 */
public class FarmingTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private FarmingTracker tracker;

    private static final int MUSHROOM_REGION = 13622;
    private static final int MUSHROOM_VARBIT = 4771;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new FarmingTracker(apiClient, client);

        Player player = mock(Player.class);
        when(player.getWorldLocation()).thenReturn(WorldPoint.fromRegion(MUSHROOM_REGION, 0, 0, 0));
        when(client.getLocalPlayer()).thenReturn(player);
    }

    private void setPatchValue(int v) {
        when(client.getVarbitValue(MUSHROOM_VARBIT)).thenReturn(v);
    }

    @Test
    public void firstCheckEstablishesABaselineWithoutEmitting() {
        setPatchValue(0); // weeds
        tracker.checkPatches();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void doesNotTouchPatchesOutsideThePlayersCurrentRegion() {
        Player player = mock(Player.class);
        when(player.getWorldLocation()).thenReturn(new WorldPoint(0, 0, 0)); // region 0, no known patches
        when(client.getLocalPlayer()).thenReturn(player);

        tracker.checkPatches();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void plantingACropOverWeedsEmitsAPlantedTransition() {
        setPatchValue(0); // WEEDS/GROWING
        tracker.checkPatches(); // baseline

        setPatchValue(5); // MUSHROOM/GROWING
        tracker.checkPatches();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.FarmingPatch event = (StatEvent.FarmingPatch) captor.getValue();
        assertEquals("Morytania (Mushroom)", event.patchName);
        assertEquals("Mushroom", event.crop);
        assertEquals("PLANTED", event.state);
    }

    @Test
    public void aFullyGrownCropEmitsAHarvestableTransition() {
        setPatchValue(5); // MUSHROOM/GROWING
        tracker.checkPatches(); // baseline

        setPatchValue(12); // MUSHROOM/HARVESTABLE
        tracker.checkPatches();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("HARVESTABLE", ((StatEvent.FarmingPatch) captor.getValue()).state);
    }

    @Test
    public void harvestingAReadyCropBackDownToWeedsEmitsAHarvestedTransition() {
        setPatchValue(12); // MUSHROOM/HARVESTABLE
        tracker.checkPatches(); // baseline

        setPatchValue(0); // WEEDS/GROWING
        tracker.checkPatches();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("HARVESTED", ((StatEvent.FarmingPatch) captor.getValue()).state);
    }

    @Test
    public void aDiseasedCropEmitsADiseasedTransition() {
        setPatchValue(5); // MUSHROOM/GROWING
        tracker.checkPatches(); // baseline

        setPatchValue(17); // MUSHROOM/DISEASED
        tracker.checkPatches();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("DISEASED", ((StatEvent.FarmingPatch) captor.getValue()).state);
    }

    @Test
    public void aDeadCropEmitsADeadTransition() {
        setPatchValue(17); // MUSHROOM/DISEASED
        tracker.checkPatches(); // baseline

        setPatchValue(22); // MUSHROOM/DEAD
        tracker.checkPatches();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("DEAD", ((StatEvent.FarmingPatch) captor.getValue()).state);
    }

    @Test
    public void doesNotReEmitWhenPolledAgainWithNoActualStateChange() {
        setPatchValue(5);
        tracker.checkPatches(); // baseline

        setPatchValue(6); // still within the MUSHROOM+GROWING range -- same decoded state
        tracker.checkPatches();

        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resetReEstablishesAFreshBaseline() {
        setPatchValue(0);
        tracker.checkPatches();
        setPatchValue(5);
        tracker.checkPatches(); // emits once

        tracker.reset();
        setPatchValue(5);
        tracker.checkPatches(); // re-baselines, no emission

        verify(apiClient, times(1)).enqueue(any());
    }
}
