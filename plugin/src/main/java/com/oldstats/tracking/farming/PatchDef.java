package com.oldstats.tracking.farming;

public final class PatchDef {
    public final String name;
    public final int varbit;
    public final PatchImplementation implementation;

    public PatchDef(String name, int varbit, PatchImplementation implementation) {
        this.name = name;
        this.varbit = varbit;
        this.implementation = implementation;
    }
}
