package com.oldstats

import com.google.gson.Gson
import com.google.inject.Provides
import com.oldstats.api.OldStatsApiClient
import com.oldstats.tracking.AchievementDiaryTracker
import com.oldstats.tracking.BankTracker
import com.oldstats.tracking.ClueTracker
import com.oldstats.tracking.CollectionLogTracker
import com.oldstats.tracking.CombatAchievementTracker
import com.oldstats.tracking.DiaryBitsetTracker
import com.oldstats.tracking.DiaryTaskTracker
import com.oldstats.tracking.FarmingTracker
import com.oldstats.tracking.LootTracker
import com.oldstats.tracking.NetWorthTracker
import com.oldstats.tracking.PersonalBestTracker
import com.oldstats.tracking.PetTracker
import com.oldstats.tracking.PvpTracker
import com.oldstats.tracking.QuestTracker
import com.oldstats.tracking.SlayerTracker
import com.oldstats.tracking.WorldTracker
import com.oldstats.tracking.XpTracker
import net.runelite.api.Client
import net.runelite.api.GameState
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.ChatMessage
import net.runelite.api.events.GameStateChanged
import net.runelite.api.events.GameTick
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.events.NpcSpawned
import net.runelite.api.events.StatChanged
import net.runelite.api.events.VarbitChanged
import net.runelite.api.events.WidgetLoaded
import net.runelite.api.events.WorldChanged
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.eventbus.Subscribe
import net.runelite.client.events.NpcLootReceived
import net.runelite.client.events.PlayerLootReceived
import net.runelite.client.game.ItemManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@PluginDescriptor(
    name = "OldStats",
    description = "Tracks XP, kills, drops, quests, slayer, farming, collection log, combat achievements, " +
        "diaries, clues, pets, PBs, PvP, worlds and net worth, and reports them to your OldStats server",
    tags = [
        "stats", "tracker", "xp", "loot", "slayer", "farming", "quests", "bosses",
        "collection log", "combat achievements", "achievement diary", "clue", "pet",
        "personal best", "pvp", "world", "net worth",
    ],
)
class OldStatsPlugin : Plugin() {

    @Inject
    private lateinit var client: Client

    @Inject
    private lateinit var config: OldStatsConfig

    @Inject
    private lateinit var configManager: ConfigManager

    @Inject
    private lateinit var okHttpClient: OkHttpClient

    @Inject
    private lateinit var gson: Gson

    @Inject
    private lateinit var itemManager: ItemManager

    @Inject
    private lateinit var executor: ScheduledExecutorService

    @Inject
    private lateinit var clientThread: ClientThread

    private lateinit var apiClient: OldStatsApiClient
    private lateinit var xpTracker: XpTracker
    private lateinit var lootTracker: LootTracker
    private lateinit var questTracker: QuestTracker
    private lateinit var slayerTracker: SlayerTracker
    private lateinit var farmingTracker: FarmingTracker
    private lateinit var collectionLogTracker: CollectionLogTracker
    private lateinit var combatAchievementTracker: CombatAchievementTracker
    private lateinit var achievementDiaryTracker: AchievementDiaryTracker
    private lateinit var diaryTaskTracker: DiaryTaskTracker
    private lateinit var diaryBitsetTracker: DiaryBitsetTracker
    private lateinit var clueTracker: ClueTracker
    private lateinit var petTracker: PetTracker
    private lateinit var pvpTracker: PvpTracker
    private lateinit var personalBestTracker: PersonalBestTracker
    private lateinit var worldTracker: WorldTracker
    private lateinit var netWorthTracker: NetWorthTracker
    private lateinit var bankTracker: BankTracker

    private var flushTask: ScheduledFuture<*>? = null
    private var netWorthTask: ScheduledFuture<*>? = null

    @Provides
    fun provideConfig(configManager: ConfigManager): OldStatsConfig =
        configManager.getConfig(OldStatsConfig::class.java)

    override fun startUp() {
        showDataWarningOnce()

        apiClient = OldStatsApiClient(
            httpClient = okHttpClient,
            gson = gson,
            serverUrlProvider = { config.serverUrl() },
            apiKeyProvider = { config.apiKey() },
        )
        xpTracker = XpTracker(apiClient)
        lootTracker = LootTracker(apiClient, itemManager)
        questTracker = QuestTracker(apiClient, client, clientThread)
        slayerTracker = SlayerTracker(apiClient, client)
        farmingTracker = FarmingTracker(apiClient, client)
        collectionLogTracker = CollectionLogTracker(apiClient, client, itemManager)
        combatAchievementTracker = CombatAchievementTracker(apiClient, client)
        achievementDiaryTracker = AchievementDiaryTracker(apiClient, client)
        diaryTaskTracker = DiaryTaskTracker(apiClient, client, clientThread)
        diaryBitsetTracker = DiaryBitsetTracker(apiClient, client)
        clueTracker = ClueTracker(apiClient)
        petTracker = PetTracker(apiClient, client)
        pvpTracker = PvpTracker(apiClient, client)
        personalBestTracker = PersonalBestTracker(apiClient, lootTracker)
        worldTracker = WorldTracker(apiClient, client)
        netWorthTracker = NetWorthTracker(apiClient, client, itemManager)
        bankTracker = BankTracker(apiClient, itemManager)

        val intervalSeconds = config.flushIntervalSeconds().coerceAtLeast(5).toLong()
        flushTask = executor.scheduleWithFixedDelay(
            { runCatching { apiClient.flush() } },
            intervalSeconds,
            intervalSeconds,
            TimeUnit.SECONDS,
        )

        val netWorthIntervalMinutes = config.netWorthIntervalMinutes().coerceAtLeast(5).toLong()
        netWorthTask = executor.scheduleWithFixedDelay(
            {
                if (config.trackNetWorth()) {
                    clientThread.invoke(Runnable { runCatching { netWorthTracker.snapshot() } })
                }
            },
            netWorthIntervalMinutes,
            netWorthIntervalMinutes,
            TimeUnit.MINUTES,
        )
    }

    override fun shutDown() {
        flushTask?.cancel(false)
        flushTask = null
        netWorthTask?.cancel(false)
        netWorthTask = null
        // flush() dispatches over OkHttp's own threadpool and returns immediately,
        // so this doesn't block shutdown waiting on the network.
        runCatching { apiClient.flush() }
    }

    /**
     * Mirrors how WikiSync discloses its own third-party data submission: a one-time
     * popup the first time the plugin runs, rather than gating every tracker behind
     * an opt-in toggle. Shown off the calling thread so startUp() never blocks on it.
     */
    private fun showDataWarningOnce() {
        val alreadyShown = configManager.getConfiguration(OldStatsConfig.GROUP, OldStatsConfig.WARNING_SHOWN_KEY)?.toBoolean() ?: false
        if (alreadyShown) return
        configManager.setConfiguration(OldStatsConfig.GROUP, OldStatsConfig.WARNING_SHOWN_KEY, true)

        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(
                null,
                "OldStats sends your tracked stats (XP, kills, drops, quests, and so on) to the " +
                    "server configured under \"Server URL\" in this plugin's settings.\n\n" +
                    "This server is not run, controlled, or verified by RuneLite developers — it's " +
                    "whatever you configure, typically a server you host yourself.\n\n" +
                    "Nothing is sent until you set a Server URL and API key.",
                "OldStats",
                JOptionPane.WARNING_MESSAGE,
            )
        }
    }

    @Subscribe
    fun onGameStateChanged(event: GameStateChanged) {
        if (event.gameState == GameState.LOGGING_IN || event.gameState == GameState.HOPPING) {
            xpTracker.reset()
            questTracker.reset()
            slayerTracker.reset()
            farmingTracker.reset()
            collectionLogTracker.reset()
            combatAchievementTracker.reset()
            achievementDiaryTracker.reset()
            worldTracker.reset()
        }
        if (event.gameState == GameState.LOGGED_IN && config.trackWorlds()) {
            worldTracker.checkWorld()
        }
    }

    @Subscribe
    fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChanged) {
        if (config.trackWorlds()) worldTracker.checkWorld()
    }

    @Subscribe
    fun onStatChanged(event: StatChanged) {
        if (config.trackXp()) xpTracker.onStatChanged(event)
    }

    @Subscribe
    fun onNpcLootReceived(event: NpcLootReceived) {
        if (config.trackLoot()) lootTracker.onNpcLootReceived(event)
    }

    @Subscribe
    fun onPlayerLootReceived(event: PlayerLootReceived) {
        if (config.trackPvp()) pvpTracker.onPlayerLootReceived(event)
    }

    @Subscribe
    fun onActorDeath(event: ActorDeath) {
        if (config.trackPvp()) pvpTracker.onActorDeath(event)
    }

    @Subscribe
    fun onNpcSpawned(event: NpcSpawned) {
        if (config.trackPets()) petTracker.onNpcSpawned(event)
    }

    @Subscribe
    fun onGameTick(event: GameTick) {
        if (config.trackPets()) petTracker.onGameTick(event)
    }

    @Subscribe
    fun onVarbitChanged(@Suppress("UNUSED_PARAMETER") event: VarbitChanged) {
        if (config.trackQuests()) questTracker.checkQuests()
        if (config.trackSlayer()) slayerTracker.checkTask()
        if (config.trackFarming()) farmingTracker.checkPatches()
        if (config.trackCollectionLog()) collectionLogTracker.checkNewestUnlock()
        if (config.trackCombatAchievements()) combatAchievementTracker.checkTasks()
        if (config.trackDiaries()) achievementDiaryTracker.checkDiaries()
        if (config.trackDiaries()) diaryBitsetTracker.checkTasks()
    }

    @Subscribe
    fun onChatMessage(event: ChatMessage) {
        if (config.trackClues()) clueTracker.onChatMessage(event)
        if (config.trackPets()) petTracker.onChatMessage(event)
        if (config.trackPersonalBests()) personalBestTracker.onChatMessage(event)
    }

    @Subscribe
    fun onItemContainerChanged(event: ItemContainerChanged) {
        if (config.trackBank()) bankTracker.onItemContainerChanged(event)
    }

    @Subscribe
    fun onWidgetLoaded(event: WidgetLoaded) {
        if (config.trackDiaries()) diaryTaskTracker.onWidgetLoaded(event)
    }
}
