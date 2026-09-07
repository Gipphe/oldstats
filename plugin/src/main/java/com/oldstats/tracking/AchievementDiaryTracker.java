package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/**
 * There is no dedicated "diary completed" event. RuneLite's generated
 * {@link VarbitID} constants expose one boolean varbit per area/tier, named
 * {@code <AREA>_DIARY_<TIER>_COMPLETE} (e.g. {@code ARDOUGNE_DIARY_EASY_COMPLETE}) —
 * this reflects over those field names once at startup and polls/diffs them
 * on every VarbitChanged tick, the same pattern as {@link QuestTracker}.
 *
 * <p>Karamja predates the tiered diary system: its easy/medium/hard tiers have
 * no {@code _COMPLETE} boolean, only task-count varbits ({@link VarbitID#KARAMJA_EASY_COUNT},
 * {@code KARAMJA_MED_COUNT}, {@code KARAMJA_HARD_COUNT}). Those are handled separately
 * below using task totals per tier (10/19/10) sourced from the OSRS Wiki's
 * Karamja Diary page — unlike the {@code _COMPLETE} varbits, these are static
 * numbers this plugin hardcodes rather than something the game exposes
 * directly, so they'd need updating if Jagex ever adds/removes Karamja
 * diary tasks.
 */
public class AchievementDiaryTracker {
    private static final class DiaryVarbit {
        final String area;
        final String tier;
        final int varbitId;

        DiaryVarbit(String area, String tier, int varbitId) {
            this.area = area;
            this.tier = tier;
            this.varbitId = varbitId;
        }
    }

    private static final class ThresholdDiary {
        final String area;
        final String tier;
        final int varbitId;
        final int requiredCount;

        ThresholdDiary(String area, String tier, int varbitId, int requiredCount) {
            this.area = area;
            this.tier = tier;
            this.varbitId = varbitId;
            this.requiredCount = requiredCount;
        }
    }

    private final OldStatsApiClient apiClient;
    private final Client client;

    private final List<DiaryVarbit> diaryVarbits;

    private final List<ThresholdDiary> karamjaThresholdDiaries = List.of(
        new ThresholdDiary("KARAMJA", "EASY", VarbitID.KARAMJA_EASY_COUNT, 10),
        new ThresholdDiary("KARAMJA", "MEDIUM", VarbitID.KARAMJA_MED_COUNT, 19),
        new ThresholdDiary("KARAMJA", "HARD", VarbitID.KARAMJA_HARD_COUNT, 10)
    );

    private final ConcurrentHashMap<Integer, Integer> lastValues = new ConcurrentHashMap<>();

    public AchievementDiaryTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
        this.diaryVarbits = buildDiaryVarbits();
    }

    private static List<DiaryVarbit> buildDiaryVarbits() {
        Pattern pattern = Pattern.compile("^(.+)_DIARY_(EASY|MEDIUM|HARD|ELITE)_COMPLETE$");
        List<DiaryVarbit> result = new ArrayList<>();
        for (Field field : VarbitID.class.getFields()) {
            Matcher match = pattern.matcher(field.getName());
            if (!match.find()) continue;
            try {
                result.add(new DiaryVarbit(match.group(1), match.group(2), field.getInt(null)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public void reset() {
        lastValues.clear();
    }

    public void checkDiaries() {
        for (DiaryVarbit diary : diaryVarbits) {
            int value = client.getVarbitValue(diary.varbitId);
            Integer previous = lastValues.put(diary.varbitId, value);
            if (previous == null) continue; // baseline only
            if (previous == 0 && value != 0) {
                apiClient.enqueue(new StatEvent.DiaryCompleted(diary.area, diary.tier));
            }
        }

        for (ThresholdDiary diary : karamjaThresholdDiaries) {
            int value = client.getVarbitValue(diary.varbitId);
            Integer previous = lastValues.put(diary.varbitId, value);
            if (previous == null) continue; // baseline only
            if (previous < diary.requiredCount && value >= diary.requiredCount) {
                apiClient.enqueue(new StatEvent.DiaryCompleted(diary.area, diary.tier));
            }
        }
    }
}
