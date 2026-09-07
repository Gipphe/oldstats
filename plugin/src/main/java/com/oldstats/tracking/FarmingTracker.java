package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import com.oldstats.tracking.farming.CropState;
import com.oldstats.tracking.farming.FarmingWorldData;
import com.oldstats.tracking.farming.PatchDecode;
import com.oldstats.tracking.farming.PatchDef;
import com.oldstats.tracking.farming.PatchState;
import com.oldstats.tracking.farming.Produce;
import com.oldstats.tracking.farming.RegionDef;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

/**
 * Tracks farming patch state the same way RuneLite's own Farming Tracker
 * (part of the Time Tracking plugin) does: matching the player's current
 * region against a ported copy of its region/patch/varbit database (see
 * {@code tracking/farming/FarmingWorldData.java}), then decoding each patch's
 * raw varbit value via a ported copy of its per-crop decode tables (see
 * {@code tracking/farming/PatchDecode.java}). Both were hand-transcribed from
 * RuneLite's decompiled source since the real classes are package-private
 * and live inside a different plugin's Guice scope.
 *
 * <p>Patch varbits are only meaningful while the player is in/near the owning
 * region (the same numeric varbit id is reused across many unrelated
 * regions), so — like the real plugin — this only reads patches belonging
 * to the region the player is currently standing in.
 */
public class FarmingTracker {
    private final OldStatsApiClient apiClient;
    private final Client client;

    private final ConcurrentHashMap<String, PatchState> lastStates = new ConcurrentHashMap<>();

    public FarmingTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
    }

    public void reset() {
        lastStates.clear();
    }

    public void checkPatches() {
        Player player = client.getLocalPlayer();
        WorldPoint loc = player != null ? player.getWorldLocation() : null;
        if (loc == null) return;

        List<RegionDef> candidates = FarmingWorldData.regionsById.get(loc.getRegionID());
        if (candidates == null) return;

        RegionDef region = null;
        for (RegionDef candidate : candidates) {
            if (candidate.boundsCheck == null || candidate.boundsCheck.test(loc.getX(), loc.getY(), loc.getPlane())) {
                region = candidate;
                break;
            }
        }
        if (region == null) return;

        for (PatchDef patch : region.patches) {
            int value = client.getVarbitValue(patch.varbit);
            PatchState decoded = PatchDecode.decodePatchState(patch.implementation, value);
            if (decoded == null) continue;

            String key = region.name + "|" + patch.name + "|" + patch.varbit;
            PatchState previous = lastStates.put(key, decoded);
            if (previous == null || previous.equals(decoded)) continue; // baseline, or no real change

            String patchLabel = patch.name.isBlank() ? region.name : region.name + " (" + patch.name + ")";
            apiClient.enqueue(
                new StatEvent.FarmingPatch(patchLabel, decoded.produce.displayName, transitionState(previous, decoded))
            );
        }
    }

    private String transitionState(PatchState previous, PatchState current) {
        if (current.cropState == CropState.EMPTY) return "EMPTY";
        if (current.cropState == CropState.DISEASED) return "DISEASED";
        if (current.cropState == CropState.DEAD) return "DEAD";
        if (current.cropState == CropState.HARVESTABLE) return "HARVESTABLE";
        if (previous.cropState == CropState.HARVESTABLE && current.produce == Produce.WEEDS) return "HARVESTED";
        if (previous.produce == Produce.WEEDS && current.produce != Produce.WEEDS) return "PLANTED";
        return "GROWING";
    }
}
