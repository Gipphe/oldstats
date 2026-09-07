package com.oldstats.tracking.farming;

import java.util.Objects;

public final class PatchState {
    public final Produce produce;
    public final CropState cropState;

    public PatchState(Produce produce, CropState cropState) {
        this.produce = produce;
        this.cropState = cropState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatchState)) return false;
        PatchState other = (PatchState) o;
        return produce == other.produce && cropState == other.cropState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(produce, cropState);
    }

    @Override
    public String toString() {
        return "PatchState(produce=" + produce + ", cropState=" + cropState + ")";
    }
}
