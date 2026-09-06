package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import com.oldstats.tracking.diary.DiaryTaskData
import com.oldstats.tracking.diary.DiaryTaskDef
import net.runelite.api.Client
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks individual achievement diary task completion continuously — no
 * need to open the diary journal, unlike [DiaryTaskTracker]. Ported from
 * RuneLite's Quest Helper plugin, which uses the same per-area VarPlayer
 * bitsets to decide which diary steps to skip (one bit per task; see
 * [DiaryTaskData] for the full ported table and its provenance).
 *
 * Unlike the baseline-suppressing pattern most other trackers use (only
 * report *changes*, not the state as of plugin startup), this reports the
 * full current state on the very first check too. The others suppress the
 * baseline because they feed a chronological "recent activity" list, where
 * back-dating a months-old completion to "today" would be misleading; this
 * feeds a status checklist instead (like [BankTracker]/[NetWorthTracker]),
 * where showing nothing at all until something *changes* would leave an
 * empty checklist for anyone who installs this after already having done
 * some tasks.
 */
class DiaryBitsetTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private val lastValues = ConcurrentHashMap<String, Boolean>()

    private val tasksByVarp: Map<Int, List<DiaryTaskDef>> by lazy {
        DiaryTaskData.ALL_TASKS.groupBy { it.varpId }
    }

    fun reset() {
        lastValues.clear()
    }

    fun checkTasks() {
        for ((varpId, tasks) in tasksByVarp) {
            val varpValue = client.getVarpValue(varpId)
            for (task in tasks) {
                val completed = ((varpValue shr task.bitPosition) and 1) == 1
                val key = "${task.varpId}#${task.bitPosition}"
                val previous = lastValues.put(key, completed)
                if (previous == completed) continue
                apiClient.enqueue(
                    StatEvent.DiaryTaskProgress(
                        diaryArea = task.area,
                        tier = task.tier,
                        taskName = task.taskName,
                        completed = completed,
                    )
                )
            }
        }
    }
}
