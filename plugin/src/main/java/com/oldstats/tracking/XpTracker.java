package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

/**
 * Tracks per-skill XP gains and level-ups from {@link StatChanged}. The first
 * observation of a skill after (re)login is used only to establish a
 * baseline — it is not reported as a gain, since login replays each skill's
 * full stored XP as a single StatChanged event.
 */
public class XpTracker {
    private final OldStatsApiClient apiClient;

    private final ConcurrentHashMap<Skill, Integer> lastXp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Skill, Integer> lastLevel = new ConcurrentHashMap<>();

    public XpTracker(OldStatsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void reset() {
        lastXp.clear();
        lastLevel.clear();
    }

    public void onStatChanged(StatChanged event) {
        Skill skill = event.getSkill();
        int xp = event.getXp();
        int level = event.getLevel();

        Integer previousXp = lastXp.put(skill, xp);
        Integer previousLevel = lastLevel.put(skill, level);

        if (previousXp == null || previousLevel == null) {
            // First sighting this session: baseline only, no event emitted.
            return;
        }

        if (xp > previousXp) {
            apiClient.enqueue(new StatEvent.XpGain(skill.name(), xp, (long) (xp - previousXp), level));
        }

        if (level > previousLevel) {
            apiClient.enqueue(new StatEvent.LevelUp(skill.name(), level));
        }
    }
}
