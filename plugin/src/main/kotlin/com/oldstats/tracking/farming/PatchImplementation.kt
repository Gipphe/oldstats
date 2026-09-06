package com.oldstats.tracking.farming

/**
 * Mirrors RuneLite's internal `PatchImplementation` enum
 * (net.runelite.client.plugins.timetracking.farming.PatchImplementation) —
 * only the crop-type identity, not its per-value decode logic (see
 * [decodePatchState] for that, in a separate file since it's large).
 */
enum class PatchImplementation {
    MUSHROOM,
    HESPORI,
    ALLOTMENT,
    HERB,
    FLOWER,
    BUSH,
    FRUIT_TREE,
    HOPS,
    TREE,
    HARDWOOD_TREE,
    REDWOOD,
    SPIRIT_TREE,
    ANIMA,
    BELLADONNA,
    CACTUS,
    CORAL,
    SEAWEED,
    CALQUAT,
    CELASTRUS,
    GRAPES,
    CRYSTAL_TREE,
    COMPOST,
    BIG_COMPOST,
}
