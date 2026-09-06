package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Skill
import net.runelite.api.events.StatChanged
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks per-skill XP gains and level-ups from [StatChanged]. The first
 * observation of a skill after (re)login is used only to establish a
 * baseline — it is not reported as a gain, since login replays each skill's
 * full stored XP as a single StatChanged event.
 */
class XpTracker(private val apiClient: OldStatsApiClient) {
    private val lastXp = ConcurrentHashMap<Skill, Int>()
    private val lastLevel = ConcurrentHashMap<Skill, Int>()

    fun reset() {
        lastXp.clear()
        lastLevel.clear()
    }

    fun onStatChanged(event: StatChanged) {
        val skill = event.skill
        val xp = event.xp
        val level = event.level

        val previousXp = lastXp.put(skill, xp)
        val previousLevel = lastLevel.put(skill, level)

        if (previousXp == null || previousLevel == null) {
            // First sighting this session: baseline only, no event emitted.
            return
        }

        if (xp > previousXp) {
            apiClient.enqueue(
                StatEvent.XpGain(
                    skill = skill.name,
                    xp = xp.toLong(),
                    xpGained = (xp - previousXp).toLong(),
                    level = level,
                )
            )
        }

        if (level > previousLevel) {
            apiClient.enqueue(StatEvent.LevelUp(skill = skill.name, level = level))
        }
    }
}
