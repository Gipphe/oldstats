package com.oldstats.tracking.farming;

import java.util.List;

public final class RegionDef {
    public final String name;
    public final List<Integer> regionIds;
    public final List<PatchDef> patches;
    public final BoundsCheck boundsCheck;

    public RegionDef(String name, List<Integer> regionIds, List<PatchDef> patches) {
        this(name, regionIds, patches, null);
    }

    public RegionDef(String name, List<Integer> regionIds, List<PatchDef> patches, BoundsCheck boundsCheck) {
        this.name = name;
        this.regionIds = regionIds;
        this.patches = patches;
        this.boundsCheck = boundsCheck;
    }
}
