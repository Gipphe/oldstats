package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.WorldType;

/**
 * Emits an event whenever the current world changes (including the very
 * first sighting each session — unlike the baseline-suppression pattern
 * used elsewhere, "I'm on this world starting now" is itself useful data
 * for the server's time-per-world breakdown, which derives duration from
 * the gap between consecutive events).
 */
public class WorldTracker {
    private final OldStatsApiClient apiClient;
    private final Client client;

    private Integer lastWorld;

    public WorldTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
    }

    public void reset() {
        lastWorld = null;
    }

    public void checkWorld() {
        int world = client.getWorld();
        if (world == 0 || Integer.valueOf(world).equals(lastWorld)) return;
        lastWorld = world;
        List<String> types = client.getWorldType().stream().map(WorldType::name).collect(Collectors.toList());
        apiClient.enqueue(new StatEvent.WorldChange(world, types));
    }
}
