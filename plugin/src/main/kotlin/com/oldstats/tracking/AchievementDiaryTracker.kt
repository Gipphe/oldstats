package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.gameval.VarbitID
import java.util.concurrent.ConcurrentHashMap

/**
 * There is no dedicated "diary completed" event. RuneLite's generated
 * [VarbitID] constants expose one boolean varbit per area/tier, named
 * `<AREA>_DIARY_<TIER>_COMPLETE` (e.g. `ARDOUGNE_DIARY_EASY_COMPLETE`) — this
 * reflects over those field names once at startup and polls/diffs them on
 * every VarbitChanged tick, the same pattern as [QuestTracker].
 *
 * Karamja predates the tiered diary system: its easy/medium/hard tiers have
 * no `_COMPLETE` boolean, only task-count varbits ([VarbitID.KARAMJA_EASY_COUNT],
 * `KARAMJA_MED_COUNT`, `KARAMJA_HARD_COUNT`). Those are handled separately
 * below using task totals per tier (10/19/10) sourced from the OSRS Wiki's
 * Karamja Diary page — unlike the `_COMPLETE` varbits, these are static
 * numbers this plugin hardcodes rather than something the game exposes
 * directly, so they'd need updating if Jagex ever adds/removes Karamja
 * diary tasks.
 */
class AchievementDiaryTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private data class DiaryVarbit(val area: String, val tier: String, val varbitId: Int)
    private data class ThresholdDiary(val area: String, val tier: String, val varbitId: Int, val requiredCount: Int)

    private val diaryVarbits: List<DiaryVarbit> by lazy {
        val pattern = Regex("^(.+)_DIARY_(EASY|MEDIUM|HARD|ELITE)_COMPLETE$")
        VarbitID::class.java.fields.mapNotNull { field ->
            val match = pattern.find(field.name) ?: return@mapNotNull null
            DiaryVarbit(area = match.groupValues[1], tier = match.groupValues[2], varbitId = field.getInt(null))
        }
    }

    private val karamjaThresholdDiaries: List<ThresholdDiary> = listOf(
        ThresholdDiary("KARAMJA", "EASY", VarbitID.KARAMJA_EASY_COUNT, requiredCount = 10),
        ThresholdDiary("KARAMJA", "MEDIUM", VarbitID.KARAMJA_MED_COUNT, requiredCount = 19),
        ThresholdDiary("KARAMJA", "HARD", VarbitID.KARAMJA_HARD_COUNT, requiredCount = 10),
    )

    private val lastValues = ConcurrentHashMap<Int, Int>()

    fun reset() {
        lastValues.clear()
    }

    fun checkDiaries() {
        for (diary in diaryVarbits) {
            val value = client.getVarbitValue(diary.varbitId)
            val previous = lastValues.put(diary.varbitId, value)
            if (previous == null) continue // baseline only
            if (previous == 0 && value != 0) {
                apiClient.enqueue(StatEvent.DiaryCompleted(diaryArea = diary.area, tier = diary.tier))
            }
        }

        for (diary in karamjaThresholdDiaries) {
            val value = client.getVarbitValue(diary.varbitId)
            val previous = lastValues.put(diary.varbitId, value)
            if (previous == null) continue // baseline only
            if (previous < diary.requiredCount && value >= diary.requiredCount) {
                apiClient.enqueue(StatEvent.DiaryCompleted(diaryArea = diary.area, tier = diary.tier))
            }
        }
    }
}
