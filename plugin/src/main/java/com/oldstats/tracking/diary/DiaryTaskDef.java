package com.oldstats.tracking.diary;

public final class DiaryTaskDef {
    public final String area;
    public final String tier;
    public final String taskName;
    public final int varpId;
    public final int bitPosition;

    public DiaryTaskDef(String area, String tier, String taskName, int varpId, int bitPosition) {
        this.area = area;
        this.tier = tier;
        this.taskName = taskName;
        this.varpId = varpId;
        this.bitPosition = bitPosition;
    }
}
