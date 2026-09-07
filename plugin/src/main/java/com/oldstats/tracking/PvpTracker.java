package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Client;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.events.PlayerLootReceived;

/**
 * Player-vs-player kills come from the same core LootManager mechanism as
 * monster kills ({@link PlayerLootReceived} mirrors {@link net.runelite.client.events.NpcLootReceived}),
 * so it's just as reliable. Own deaths use {@link ActorDeath} filtered to the
 * local player; value lost on death isn't computed (kept items via Protect
 * Item, skull status, etc. make an accurate figure nontrivial), so only the
 * death itself is recorded.
 */
public class PvpTracker {
    private final OldStatsApiClient apiClient;
    private final Client client;

    public PvpTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
    }

    public void onPlayerLootReceived(PlayerLootReceived event) {
        String opponentName = event.getPlayer().getName() != null ? event.getPlayer().getName() : "Unknown";
        apiClient.enqueue(new StatEvent.PlayerKill(opponentName));
    }

    public void onActorDeath(ActorDeath event) {
        if (event.getActor() == client.getLocalPlayer()) {
            apiClient.enqueue(new StatEvent.PlayerDeath(null));
        }
    }
}
