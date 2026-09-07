package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.time.Duration;
import java.time.Instant;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;

/**
 * Tracks monster kills and the loot they drop via {@link NpcLootReceived},
 * which is posted by RuneLite's core LootManager service whenever a tracked
 * NPC's death is correlated with nearby item spawns — independent of
 * whether the built-in Loot Tracker plugin is enabled.
 */
public class LootTracker {
    private final OldStatsApiClient apiClient;
    private final ItemManager itemManager;

    private String lastBossKillName;
    private Instant lastBossKillAt;

    public LootTracker(OldStatsApiClient apiClient, ItemManager itemManager) {
        this.apiClient = apiClient;
        this.itemManager = itemManager;
    }

    public void onNpcLootReceived(NpcLootReceived event) {
        NPC npc = event.getNpc();
        String npcName = npc.getName() != null ? npc.getName() : "Unknown";
        boolean isBoss = BossList.isBoss(npcName);

        apiClient.enqueue(new StatEvent.Kill(npcName, npc.getId(), isBoss));

        if (isBoss) {
            lastBossKillName = npcName;
            lastBossKillAt = Instant.now();
        }

        for (ItemStack item : event.getItems()) {
            ItemComposition composition = itemManager.getItemComposition(item.getId());
            int price = itemManager.getItemPrice(item.getId());
            apiClient.enqueue(
                new StatEvent.Drop(npcName, composition.getName(), item.getId(), item.getQuantity(), (long) price * item.getQuantity())
            );
        }
    }

    /** Used by {@link PersonalBestTracker} to attribute a "new personal best" chat message to a boss. */
    public String recentBossKill(Duration within) {
        String name = lastBossKillName;
        Instant at = lastBossKillAt;
        if (name == null || at == null) return null;
        return Duration.between(at, Instant.now()).compareTo(within) <= 0 ? name : null;
    }
}
