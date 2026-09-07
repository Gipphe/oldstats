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
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.events.PlayerLootReceived;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PvpTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private PvpTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new PvpTracker(apiClient, client);
    }

    @Test
    public void recordsTheOpponentNameFromAPlayerKill() {
        Player opponent = mock(Player.class);
        when(opponent.getName()).thenReturn("Some Pker");

        tracker.onPlayerLootReceived(new PlayerLootReceived(opponent, Collections.emptyList()));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("Some Pker", ((StatEvent.PlayerKill) captor.getValue()).opponentName);
    }

    @Test
    public void fallsBackToUnknownWhenTheOpponentHasNoName() {
        Player opponent = mock(Player.class);
        when(opponent.getName()).thenReturn(null);

        tracker.onPlayerLootReceived(new PlayerLootReceived(opponent, Collections.emptyList()));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("Unknown", ((StatEvent.PlayerKill) captor.getValue()).opponentName);
    }

    @Test
    public void recordsADeathWhenTheLocalPlayerDies() {
        Player localPlayer = mock(Player.class);
        when(client.getLocalPlayer()).thenReturn(localPlayer);

        tracker.onActorDeath(new ActorDeath(localPlayer));

        verify(apiClient).enqueue(any(StatEvent.PlayerDeath.class));
    }

    @Test
    public void ignoresAnotherActorDying() {
        Player localPlayer = mock(Player.class);
        Actor someNpc = mock(Actor.class);
        when(client.getLocalPlayer()).thenReturn(localPlayer);

        tracker.onActorDeath(new ActorDeath(someNpc));

        verify(apiClient, never()).enqueue(any());
    }
}
