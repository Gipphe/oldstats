package com.oldstats.tracking.farming;

import static com.oldstats.tracking.farming.PatchDecode.decodePatchState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class FarmingDataTest {

    @Test
    public void everyRegionHasAtLeastOnePatch() {
        for (RegionDef region : FarmingWorldData.regions) {
            assertTrue(region.name + " has no patches", !region.patches.isEmpty());
        }
    }

    @Test
    public void catherbyRegionResolvesAndHasTheExpectedPatchSet() {
        List<RegionDef> candidates = FarmingWorldData.regionsById.get(11062);
        assertEquals(1, candidates.size());
        RegionDef region = candidates.get(0);
        assertEquals("Catherby", region.name);
        Set<PatchImplementation> implementations = region.patches.stream()
            .map(p -> p.implementation)
            .collect(Collectors.toSet());
        assertEquals(
            Set.of(PatchImplementation.ALLOTMENT, PatchImplementation.FLOWER, PatchImplementation.HERB, PatchImplementation.COMPOST),
            implementations
        );
    }

    @Test
    public void catherbyNorthAndSouthAllotmentDisambiguationUsesBoundsCheck() {
        List<RegionDef> candidates = FarmingWorldData.regionsById.get(11317);
        assertEquals(2, candidates.size());

        // Inside the allotment/herb/flower area (south of y=3440 within the x>=2816 band)
        RegionDef allotmentRegion = candidates.stream()
            .filter(r -> r.boundsCheck != null && r.boundsCheck.test(2820, 3441, 0))
            .findFirst()
            .orElseThrow();
        assertTrue(allotmentRegion.patches.stream().anyMatch(p -> p.implementation == PatchImplementation.HERB));

        // The fruit tree patch sits north of that line
        RegionDef fruitTreeRegion = candidates.stream()
            .filter(r -> r.boundsCheck != null && r.boundsCheck.test(2820, 3300, 0))
            .findFirst()
            .orElseThrow();
        assertTrue(fruitTreeRegion.patches.stream().anyMatch(p -> p.implementation == PatchImplementation.FRUIT_TREE));
    }

    @Test
    public void faladorAllotmentOnlyResolvesSouthOfTheYBoundsCheck() {
        List<RegionDef> candidates = FarmingWorldData.regionsById.get(12083);
        RegionDef region = candidates.stream()
            .filter(r -> r.patches.stream().anyMatch(p -> p.implementation == PatchImplementation.ALLOTMENT))
            .findFirst()
            .orElseThrow();
        assertEquals(true, region.boundsCheck.test(0, 3272, 0));
        assertEquals(false, region.boundsCheck.test(0, 3271, 0));
    }

    @Test
    public void farmingGuildHasThirteenDistinctPatches() {
        List<RegionDef> candidates = FarmingWorldData.regionsById.get(4922);
        assertEquals(1, candidates.size());
        assertEquals(13, candidates.get(0).patches.size());
    }

    @Test
    public void herbPatchDecodesRanarrGrowingAndHarvestableCorrectly() {
        // Ranarr: growing 32-35, harvestable 36-38 (see PatchDecode.java / real HERB table)
        assertEquals(new PatchState(Produce.RANARR, CropState.GROWING), decodePatchState(PatchImplementation.HERB, 33));
        assertEquals(new PatchState(Produce.RANARR, CropState.HARVESTABLE), decodePatchState(PatchImplementation.HERB, 37));
        assertEquals(new PatchState(Produce.RANARR, CropState.DISEASED), decodePatchState(PatchImplementation.HERB, 141));
    }

    @Test
    public void allotmentPatchDecodesPotatoLifecycleCorrectly() {
        assertEquals(new PatchState(Produce.WEEDS, CropState.GROWING), decodePatchState(PatchImplementation.ALLOTMENT, 0));
        assertEquals(new PatchState(Produce.POTATO, CropState.GROWING), decodePatchState(PatchImplementation.ALLOTMENT, 7));
        assertEquals(new PatchState(Produce.POTATO, CropState.HARVESTABLE), decodePatchState(PatchImplementation.ALLOTMENT, 11));
        assertEquals(new PatchState(Produce.POTATO, CropState.DISEASED), decodePatchState(PatchImplementation.ALLOTMENT, 136));
        assertEquals(new PatchState(Produce.POTATO, CropState.DEAD), decodePatchState(PatchImplementation.ALLOTMENT, 200));
    }

    @Test
    public void compostBinDecodesEmptyAndFillingCorrectly() {
        assertEquals(new PatchState(Produce.EMPTY_COMPOST_BIN, CropState.EMPTY), decodePatchState(PatchImplementation.COMPOST, 0));
        assertEquals(new PatchState(Produce.COMPOST, CropState.FILLING), decodePatchState(PatchImplementation.COMPOST, 5));
        assertEquals(new PatchState(Produce.COMPOST, CropState.HARVESTABLE), decodePatchState(PatchImplementation.COMPOST, 20));
    }

    @Test
    public void everyPatchVarbitReferencedByARegionHasDecodeCoverageForAtLeastValue0() {
        for (RegionDef region : FarmingWorldData.regions) {
            for (PatchDef patch : region.patches) {
                assertNotNull(
                    region.name + " " + patch.name + " (" + patch.implementation + ") has no decode for value 0",
                    decodePatchState(patch.implementation, 0)
                );
            }
        }
    }

    @Test
    public void unknownRegionResolvesToNothing() {
        assertNull(FarmingWorldData.regionsById.get(999999));
    }
}
