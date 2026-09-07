package com.oldstats;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.oldstats.api.OldStatsApiClient;
import com.oldstats.tracking.AchievementDiaryTracker;
import com.oldstats.tracking.BankTracker;
import com.oldstats.tracking.ClueTracker;
import com.oldstats.tracking.CollectionLogTracker;
import com.oldstats.tracking.CombatAchievementTracker;
import com.oldstats.tracking.DiaryBitsetTracker;
import com.oldstats.tracking.DiaryTaskTracker;
import com.oldstats.tracking.FarmingTracker;
import com.oldstats.tracking.LootTracker;
import com.oldstats.tracking.NetWorthTracker;
import com.oldstats.tracking.PersonalBestTracker;
import com.oldstats.tracking.PetTracker;
import com.oldstats.tracking.PvpTracker;
import com.oldstats.tracking.QuestTracker;
import com.oldstats.tracking.SlayerTracker;
import com.oldstats.tracking.WorldTracker;
import com.oldstats.tracking.XpTracker;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;

@PluginDescriptor(
    name = "OldStats",
    description = "Tracks XP, kills, drops, quests, slayer, farming, collection log, combat achievements, "
        + "diaries, clues, pets, PBs, PvP, worlds and net worth, and reports them to your OldStats server",
    tags = {
        "stats", "tracker", "xp", "loot", "slayer", "farming", "quests", "bosses",
        "collection log", "combat achievements", "achievement diary", "clue", "pet",
        "personal best", "pvp", "world", "net worth"
    }
)
public class OldStatsPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OldStatsConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private Gson gson;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    private ClientThread clientThread;

    private OldStatsApiClient apiClient;
    private XpTracker xpTracker;
    private LootTracker lootTracker;
    private QuestTracker questTracker;
    private SlayerTracker slayerTracker;
    private FarmingTracker farmingTracker;
    private CollectionLogTracker collectionLogTracker;
    private CombatAchievementTracker combatAchievementTracker;
    private AchievementDiaryTracker achievementDiaryTracker;
    private DiaryTaskTracker diaryTaskTracker;
    private DiaryBitsetTracker diaryBitsetTracker;
    private ClueTracker clueTracker;
    private PetTracker petTracker;
    private PvpTracker pvpTracker;
    private PersonalBestTracker personalBestTracker;
    private WorldTracker worldTracker;
    private NetWorthTracker netWorthTracker;
    private BankTracker bankTracker;

    private ScheduledFuture<?> flushTask;
    private ScheduledFuture<?> netWorthTask;

    @Provides
    OldStatsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OldStatsConfig.class);
    }

    @Override
    protected void startUp() {
        showDataWarningOnce();

        apiClient = new OldStatsApiClient(okHttpClient, gson, config::serverUrl, config::apiKey);
        xpTracker = new XpTracker(apiClient);
        lootTracker = new LootTracker(apiClient, itemManager);
        questTracker = new QuestTracker(apiClient, client, clientThread);
        slayerTracker = new SlayerTracker(apiClient, client);
        farmingTracker = new FarmingTracker(apiClient, client);
        collectionLogTracker = new CollectionLogTracker(apiClient, client, itemManager);
        combatAchievementTracker = new CombatAchievementTracker(apiClient, client);
        achievementDiaryTracker = new AchievementDiaryTracker(apiClient, client);
        diaryTaskTracker = new DiaryTaskTracker(apiClient, client, clientThread);
        diaryBitsetTracker = new DiaryBitsetTracker(apiClient, client);
        clueTracker = new ClueTracker(apiClient);
        petTracker = new PetTracker(apiClient, client);
        pvpTracker = new PvpTracker(apiClient, client);
        personalBestTracker = new PersonalBestTracker(apiClient, lootTracker);
        worldTracker = new WorldTracker(apiClient, client);
        netWorthTracker = new NetWorthTracker(apiClient, client, itemManager);
        bankTracker = new BankTracker(apiClient, itemManager);

        long intervalSeconds = Math.max(config.flushIntervalSeconds(), 5);
        flushTask = executor.scheduleWithFixedDelay(
            () -> {
                try {
                    apiClient.flush();
                } catch (Throwable ignored) {
                    // scheduled tasks that throw stop being rescheduled; never let this kill the task.
                }
            },
            intervalSeconds,
            intervalSeconds,
            TimeUnit.SECONDS
        );

        long netWorthIntervalMinutes = Math.max(config.netWorthIntervalMinutes(), 5);
        netWorthTask = executor.scheduleWithFixedDelay(
            () -> {
                if (config.trackNetWorth()) {
                    clientThread.invoke(() -> {
                        try {
                            netWorthTracker.snapshot();
                        } catch (Throwable ignored) {
                            // see above
                        }
                    });
                }
            },
            netWorthIntervalMinutes,
            netWorthIntervalMinutes,
            TimeUnit.MINUTES
        );
    }

    @Override
    protected void shutDown() {
        if (flushTask != null) {
            flushTask.cancel(false);
            flushTask = null;
        }
        if (netWorthTask != null) {
            netWorthTask.cancel(false);
            netWorthTask = null;
        }
        // flush() dispatches over OkHttp's own threadpool and returns immediately,
        // so this doesn't block shutdown waiting on the network.
        try {
            apiClient.flush();
        } catch (Throwable ignored) {
            // see startUp()
        }
    }

    /**
     * Mirrors how WikiSync discloses its own third-party data submission: a one-time
     * popup the first time the plugin runs, rather than gating every tracker behind
     * an opt-in toggle. Shown off the calling thread so startUp() never blocks on it.
     */
    private void showDataWarningOnce() {
        String stored = configManager.getConfiguration(OldStatsConfig.GROUP, OldStatsConfig.WARNING_SHOWN_KEY);
        boolean alreadyShown = stored != null && Boolean.parseBoolean(stored);
        if (alreadyShown) return;
        configManager.setConfiguration(OldStatsConfig.GROUP, OldStatsConfig.WARNING_SHOWN_KEY, true);

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
            null,
            "OldStats sends your tracked stats (XP, kills, drops, quests, and so on) to the "
                + "server configured under \"Server URL\" in this plugin's settings.\n\n"
                + "This server is not run, controlled, or verified by RuneLite developers — it's "
                + "whatever you configure, typically a server you host yourself.\n\n"
                + "Nothing is sent until you set a Server URL and API key.",
            "OldStats",
            JOptionPane.WARNING_MESSAGE
        ));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGING_IN || event.getGameState() == GameState.HOPPING) {
            xpTracker.reset();
            questTracker.reset();
            slayerTracker.reset();
            farmingTracker.reset();
            collectionLogTracker.reset();
            combatAchievementTracker.reset();
            achievementDiaryTracker.reset();
            worldTracker.reset();
        }
        if (event.getGameState() == GameState.LOGGED_IN && config.trackWorlds()) {
            worldTracker.checkWorld();
        }
    }

    @Subscribe
    public void onWorldChanged(WorldChanged event) {
        if (config.trackWorlds()) worldTracker.checkWorld();
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (config.trackXp()) xpTracker.onStatChanged(event);
    }

    @Subscribe
    public void onNpcLootReceived(NpcLootReceived event) {
        if (config.trackLoot()) lootTracker.onNpcLootReceived(event);
    }

    @Subscribe
    public void onPlayerLootReceived(PlayerLootReceived event) {
        if (config.trackPvp()) pvpTracker.onPlayerLootReceived(event);
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (config.trackPvp()) pvpTracker.onActorDeath(event);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (config.trackPets()) petTracker.onNpcSpawned(event);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (config.trackPets()) petTracker.onGameTick(event);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (config.trackQuests()) questTracker.checkQuests();
        if (config.trackSlayer()) slayerTracker.checkTask();
        if (config.trackFarming()) farmingTracker.checkPatches();
        if (config.trackCollectionLog()) collectionLogTracker.checkNewestUnlock();
        if (config.trackCombatAchievements()) combatAchievementTracker.checkTasks();
        if (config.trackDiaries()) achievementDiaryTracker.checkDiaries();
        if (config.trackDiaries()) diaryBitsetTracker.checkTasks();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (config.trackClues()) clueTracker.onChatMessage(event);
        if (config.trackPets()) petTracker.onChatMessage(event);
        if (config.trackPersonalBests()) personalBestTracker.onChatMessage(event);
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (config.trackBank()) bankTracker.onItemContainerChanged(event);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (config.trackDiaries()) diaryTaskTracker.onWidgetLoaded(event);
    }
}
