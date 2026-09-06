package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.ItemContainer
import net.runelite.api.gameval.InventoryID
import net.runelite.client.game.ItemManager

/**
 * Periodic (not event-driven) snapshot of inventory + equipment + bank
 * value. Must be invoked on the client thread (via `ClientThread.invoke`),
 * same as any other read of [Client] state — the caller is responsible for
 * that, this class assumes it's already true. Bank value is only accurate
 * once the player has opened their bank at least once this session, since
 * that's when the client actually receives bank contents from the server;
 * this is a RuneLite-wide limitation, not specific to this plugin.
 */
class NetWorthTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
    private val itemManager: ItemManager,
) {
    fun snapshot() {
        val inventoryValue = valueOf(client.getItemContainer(InventoryID.INV))
        val equipmentValue = valueOf(client.getItemContainer(InventoryID.WORN))
        val bankValue = valueOf(client.getItemContainer(InventoryID.BANK))
        val total = inventoryValue + equipmentValue + bankValue
        if (total == 0L) return

        apiClient.enqueue(
            StatEvent.NetWorthSnapshot(
                inventoryValue = inventoryValue,
                equipmentValue = equipmentValue,
                bankValue = bankValue,
                totalValue = total,
            )
        )
    }

    private fun valueOf(container: ItemContainer?): Long {
        if (container == null) return 0L
        var total = 0L
        for (item in container.items) {
            if (item.id <= 0) continue
            total += itemManager.getItemPrice(item.id).toLong() * item.quantity
        }
        return total
    }
}
