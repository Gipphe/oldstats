package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import com.oldstats.tracking.farming.CropState
import com.oldstats.tracking.farming.FarmingWorldData
import com.oldstats.tracking.farming.PatchState
import com.oldstats.tracking.farming.Produce
import com.oldstats.tracking.farming.decodePatchState
import net.runelite.api.Client
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks farming patch state the same way RuneLite's own Farming Tracker
 * (part of the Time Tracking plugin) does: matching the player's current
 * region against a ported copy of its region/patch/varbit database (see
 * `tracking/farming/FarmingWorldData.kt`), then decoding each patch's raw
 * varbit value via a ported copy of its per-crop decode tables (see
 * `tracking/farming/PatchDecode.kt`). Both were hand-transcribed from
 * RuneLite's decompiled source since the real classes are package-private
 * and live inside a different plugin's Guice scope.
 *
 * Patch varbits are only meaningful while the player is in/near the owning
 * region (the same numeric varbit id is reused across many unrelated
 * regions), so — like the real plugin — this only reads patches belonging
 * to the region the player is currently standing in.
 */
class FarmingTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private val lastStates = ConcurrentHashMap<String, PatchState>()

    fun reset() {
        lastStates.clear()
    }

    fun checkPatches() {
        val loc = client.localPlayer?.worldLocation ?: return
        val candidates = FarmingWorldData.regionsById[loc.regionID] ?: return
        val region = candidates.firstOrNull { it.boundsCheck?.invoke(loc.x, loc.y, loc.plane) ?: true } ?: return

        for (patch in region.patches) {
            val value = client.getVarbitValue(patch.varbit)
            val decoded = decodePatchState(patch.implementation, value) ?: continue
            val key = "${region.name}|${patch.name}|${patch.varbit}"
            val previous = lastStates.put(key, decoded)
            if (previous == null || previous == decoded) continue // baseline, or no real change

            val patchLabel = if (patch.name.isBlank()) region.name else "${region.name} (${patch.name})"
            apiClient.enqueue(
                StatEvent.FarmingPatch(
                    patchName = patchLabel,
                    crop = decoded.produce.displayName,
                    state = transitionState(previous, decoded),
                )
            )
        }
    }

    private fun transitionState(previous: PatchState, current: PatchState): String = when {
        current.cropState == CropState.EMPTY -> "EMPTY"
        current.cropState == CropState.DISEASED -> "DISEASED"
        current.cropState == CropState.DEAD -> "DEAD"
        current.cropState == CropState.HARVESTABLE -> "HARVESTABLE"
        previous.cropState == CropState.HARVESTABLE && current.produce == Produce.WEEDS -> "HARVESTED"
        previous.produce == Produce.WEEDS && current.produce != Produce.WEEDS -> "PLANTED"
        else -> "GROWING"
    }
}
