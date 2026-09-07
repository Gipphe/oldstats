package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import com.oldstats.tracking.diary.DiaryTaskData;
import com.oldstats.tracking.diary.DiaryTaskDef;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.runelite.api.Client;

/**
 * Tracks individual achievement diary task completion continuously — no
 * need to open the diary journal, unlike {@link DiaryTaskTracker}. Ported from
 * RuneLite's Quest Helper plugin, which uses the same per-area VarPlayer
 * bitsets to decide which diary steps to skip (one bit per task; see
 * {@link DiaryTaskData} for the full ported table and its provenance).
 *
 * <p>Unlike the baseline-suppressing pattern most other trackers use (only
 * report *changes*, not the state as of plugin startup), this reports the
 * full current state on the very first check too. The others suppress the
 * baseline because they feed a chronological "recent activity" list, where
 * back-dating a months-old completion to "today" would be misleading; this
 * feeds a status checklist instead (like {@link BankTracker}/{@link NetWorthTracker}),
 * where showing nothing at all until something *changes* would leave an
 * empty checklist for anyone who installs this after already having done
 * some tasks.
 */
public class DiaryBitsetTracker {
    private final OldStatsApiClient apiClient;
    private final Client client;

    private final ConcurrentHashMap<String, Boolean> lastValues = new ConcurrentHashMap<>();

    private final Map<Integer, List<DiaryTaskDef>> tasksByVarp;

    public DiaryBitsetTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
        this.tasksByVarp = DiaryTaskData.ALL_TASKS.stream()
            .collect(Collectors.groupingBy(t -> t.varpId, LinkedHashMap::new, Collectors.toList()));
    }

    public void reset() {
        lastValues.clear();
    }

    public void checkTasks() {
        for (Map.Entry<Integer, List<DiaryTaskDef>> entry : tasksByVarp.entrySet()) {
            int varpValue = client.getVarpValue(entry.getKey());
            for (DiaryTaskDef task : entry.getValue()) {
                boolean completed = ((varpValue >> task.bitPosition) & 1) == 1;
                String key = task.varpId + "#" + task.bitPosition;
                Boolean previous = lastValues.put(key, completed);
                if (previous != null && previous == completed) continue;
                apiClient.enqueue(
                    new StatEvent.DiaryTaskProgress(task.area, task.tier, task.taskName, completed)
                );
            }
        }
    }
}
