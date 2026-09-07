package com.oldstats.tracking.farming;

@FunctionalInterface
public interface BoundsCheck {
    boolean test(int x, int y, int plane);
}
