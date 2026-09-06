package com.oldstats

import net.runelite.client.config.Config
import net.runelite.client.config.ConfigGroup
import net.runelite.client.config.ConfigItem

@ConfigGroup("oldstats")
interface OldStatsConfig : Config {

    @ConfigItem(
        keyName = "serverUrl",
        name = "Server URL",
        description = "Base URL of your OldStats server, e.g. http://localhost:4000",
        position = 0,
    )
    fun serverUrl(): String = "http://localhost:4000"

    @ConfigItem(
        keyName = "apiKey",
        name = "API key",
        description = "API key issued by the OldStats server for this character (POST /api/players)",
        position = 1,
        secret = true,
    )
    fun apiKey(): String = ""

    @ConfigItem(
        keyName = "flushIntervalSeconds",
        name = "Send interval (seconds)",
        description = "How often queued events are flushed to the server",
        position = 2,
    )
    fun flushIntervalSeconds(): Int = 30

    @ConfigItem(
        keyName = "trackXp",
        name = "Track XP and levels",
        description = "Track skill XP gains and level-ups",
        position = 3,
    )
    fun trackXp(): Boolean = true

    @ConfigItem(
        keyName = "trackLoot",
        name = "Track kills and drops",
        description = "Track monster kills and item drops",
        position = 4,
    )
    fun trackLoot(): Boolean = true

    @ConfigItem(
        keyName = "trackQuests",
        name = "Track quests",
        description = "Track quest completions",
        position = 5,
    )
    fun trackQuests(): Boolean = true

    @ConfigItem(
        keyName = "trackSlayer",
        name = "Track slayer tasks",
        description = "Track slayer task assignment and completion",
        position = 6,
    )
    fun trackSlayer(): Boolean = true

    @ConfigItem(
        keyName = "trackFarming",
        name = "Track farming patches",
        description = "Track farming patch planting/harvesting (only while you're near the patch)",
        position = 7,
    )
    fun trackFarming(): Boolean = true

    @ConfigItem(
        keyName = "trackCollectionLog",
        name = "Track collection log",
        description = "Track new collection log item unlocks",
        position = 8,
    )
    fun trackCollectionLog(): Boolean = true

    @ConfigItem(
        keyName = "trackCombatAchievements",
        name = "Track combat achievements",
        description = "Track combat achievement task completions (task names are derived, not Jagex's exact titles)",
        position = 9,
    )
    fun trackCombatAchievements(): Boolean = true

    @ConfigItem(
        keyName = "trackDiaries",
        name = "Track achievement diaries",
        description = "Track achievement diary tier completions",
        position = 10,
    )
    fun trackDiaries(): Boolean = true

    @ConfigItem(
        keyName = "trackClues",
        name = "Track clue scrolls",
        description = "Track clue scroll completions",
        position = 11,
    )
    fun trackClues(): Boolean = true

    @ConfigItem(
        keyName = "trackPets",
        name = "Track pets",
        description = "Track pet drops (name resolution is best-effort)",
        position = 12,
    )
    fun trackPets(): Boolean = true

    @ConfigItem(
        keyName = "trackPersonalBests",
        name = "Track personal bests",
        description = "Track new boss personal bests (requires the in-game \"Fight duration\" chat setting)",
        position = 13,
    )
    fun trackPersonalBests(): Boolean = true

    @ConfigItem(
        keyName = "trackPvp",
        name = "Track PvP kills/deaths",
        description = "Track player-vs-player kills and your own deaths",
        position = 14,
    )
    fun trackPvp(): Boolean = true

    @ConfigItem(
        keyName = "trackWorlds",
        name = "Track world changes",
        description = "Track which worlds you play on and how long, for the server's per-world breakdown",
        position = 15,
    )
    fun trackWorlds(): Boolean = true

    @ConfigItem(
        keyName = "trackNetWorth",
        name = "Track net worth",
        description = "Periodically snapshot inventory + equipment + bank value",
        position = 16,
    )
    fun trackNetWorth(): Boolean = true

    @ConfigItem(
        keyName = "netWorthIntervalMinutes",
        name = "Net worth snapshot interval (minutes)",
        description = "How often to snapshot net worth",
        position = 17,
    )
    fun netWorthIntervalMinutes(): Int = 30

    @ConfigItem(
        keyName = "trackBank",
        name = "Track bank contents",
        description = "Snapshot bank contents whenever it changes, for the web app's bank view",
        position = 18,
    )
    fun trackBank(): Boolean = true
}
