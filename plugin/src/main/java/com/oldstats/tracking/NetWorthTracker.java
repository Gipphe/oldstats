package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;

/**
 * Periodic (not event-driven) snapshot of inventory + equipment + bank
 * value. Must be invoked on the client thread (via {@code ClientThread.invoke}),
 * same as any other read of {@link Client} state — the caller is responsible
 * for that, this class assumes it's already true. Bank value is only
 * accurate once the player has opened their bank at least once this
 * session, since that's when the client actually receives bank contents
 * from the server; this is a RuneLite-wide limitation, not specific to this
 * plugin.
 */
public class NetWorthTracker {
    private final OldStatsApiClient apiClient;
    private final Client client;
    private final ItemManager itemManager;

    public NetWorthTracker(OldStatsApiClient apiClient, Client client, ItemManager itemManager) {
        this.apiClient = apiClient;
        this.client = client;
        this.itemManager = itemManager;
    }

    public void snapshot() {
        long inventoryValue = valueOf(client.getItemContainer(InventoryID.INV));
        long equipmentValue = valueOf(client.getItemContainer(InventoryID.WORN));
        long bankValue = valueOf(client.getItemContainer(InventoryID.BANK));
        long total = inventoryValue + equipmentValue + bankValue;
        if (total == 0L) return;

        apiClient.enqueue(new StatEvent.NetWorthSnapshot(inventoryValue, equipmentValue, bankValue, total));
    }

    private long valueOf(ItemContainer container) {
        if (container == null) return 0L;
        long total = 0L;
        for (Item item : container.getItems()) {
            if (item.getId() <= 0) continue;
            total += (long) itemManager.getItemPrice(item.getId()) * item.getQuantity();
        }
        return total;
    }
}
