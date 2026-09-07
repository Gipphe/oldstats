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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class WorldTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private WorldTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new WorldTracker(apiClient, client);
    }

    private void stubWorld(int world) {
        stubWorld(world, EnumSet.noneOf(WorldType.class));
    }

    private void stubWorld(int world, EnumSet<WorldType> types) {
        when(client.getWorld()).thenReturn(world);
        when(client.getWorldType()).thenReturn(types);
    }

    @Test
    public void emitsOnTheVeryFirstWorldSightingUnlikeBaselineSuppressingTrackers() {
        stubWorld(420);
        tracker.checkWorld();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.WorldChange event = (StatEvent.WorldChange) captor.getValue();
        assertEquals(420, event.world);
    }

    @Test
    public void doesNotReEmitWhenTheWorldIsUnchanged() {
        stubWorld(420);
        tracker.checkWorld();
        tracker.checkWorld();
        verify(apiClient, times(1)).enqueue(any());
    }

    @Test
    public void emitsAgainWhenTheWorldChangesIncludingItsTypes() {
        stubWorld(420);
        tracker.checkWorld();
        stubWorld(421, EnumSet.of(WorldType.MEMBERS, WorldType.PVP));
        tracker.checkWorld();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(2)).enqueue(captor.capture());
        StatEvent.WorldChange second = (StatEvent.WorldChange) captor.getAllValues().get(1);
        assertEquals(421, second.world);
        assertEquals(Set.of("MEMBERS", "PVP"), new HashSet<>(second.worldTypes));
    }

    @Test
    public void ignoresWorld0TheNotYetConnectedSentinel() {
        stubWorld(0);
        tracker.checkWorld();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resetAllowsTheSameWorldToBeReEmittedEgAfterAFreshLogin() {
        stubWorld(420);
        tracker.checkWorld();
        tracker.reset();
        tracker.checkWorld();
        verify(apiClient, times(2)).enqueue(any());
    }
}
