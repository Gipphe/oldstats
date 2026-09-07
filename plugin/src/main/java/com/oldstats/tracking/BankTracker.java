package com.oldstats.tracking;

import com.oldstats.api.BankItemPayload;
import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;

/**
 * Snapshots the player's bank contents whenever it changes (opening the
 * bank populates it for the first time; RuneLite only receives bank
 * contents from the server once it's been opened at least once that
 * session, same limitation as {@link NetWorthTracker}). Rate-limited to at
 * most once per {@link #MIN_INTERVAL} so rapidly reorganizing a bank
 * doesn't spam a full item-list payload on every slot change.
 */
public class BankTracker {
    private static final Duration MIN_INTERVAL = Duration.ofSeconds(10);

    private final OldStatsApiClient apiClient;
    private final ItemManager itemManager;

    private Instant lastSentAt;

    public BankTracker(OldStatsApiClient apiClient, ItemManager itemManager) {
        this.apiClient = apiClient;
        this.itemManager = itemManager;
    }

    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != InventoryID.BANK) return;

        Instant last = lastSentAt;
        if (last != null && Duration.between(last, Instant.now()).compareTo(MIN_INTERVAL) < 0) return;
        lastSentAt = Instant.now();

        long totalValue = 0L;
        List<BankItemPayload> items = new ArrayList<>(event.getItemContainer().size());
        for (Item item : event.getItemContainer().getItems()) {
            if (item.getId() <= 0 || item.getQuantity() <= 0) continue;
            ItemComposition composition = itemManager.getItemComposition(item.getId());
            long price = itemManager.getItemPrice(item.getId());
            long value = price * item.getQuantity();
            totalValue += value;
            items.add(new BankItemPayload(item.getId(), composition.getName(), item.getQuantity(), value));
        }

        apiClient.enqueue(new StatEvent.BankSnapshot(items, totalValue));
    }
}
