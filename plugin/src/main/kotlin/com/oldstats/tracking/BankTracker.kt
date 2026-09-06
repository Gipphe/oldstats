package com.oldstats.tracking

import com.oldstats.api.BankItemPayload
import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.gameval.InventoryID
import net.runelite.client.game.ItemManager
import java.time.Duration
import java.time.Instant

/**
 * Snapshots the player's bank contents whenever it changes (opening the
 * bank populates it for the first time; RuneLite only receives bank
 * contents from the server once it's been opened at least once that
 * session, same limitation as [NetWorthTracker]). Rate-limited to at most
 * once per [MIN_INTERVAL] so rapidly reorganizing a bank doesn't spam a
 * full item-list payload on every slot change.
 */
class BankTracker(
    private val apiClient: OldStatsApiClient,
    private val itemManager: ItemManager,
) {
    private var lastSentAt: Instant? = null

    companion object {
        private val MIN_INTERVAL: Duration = Duration.ofSeconds(10)
    }

    fun onItemContainerChanged(event: ItemContainerChanged) {
        if (event.containerId != InventoryID.BANK) return

        val last = lastSentAt
        if (last != null && Duration.between(last, Instant.now()) < MIN_INTERVAL) return
        lastSentAt = Instant.now()

        var totalValue = 0L
        val items = ArrayList<BankItemPayload>(event.itemContainer.size())
        for (item in event.itemContainer.items) {
            if (item.id <= 0 || item.quantity <= 0) continue
            val composition = itemManager.getItemComposition(item.id)
            val price = itemManager.getItemPrice(item.id).toLong()
            val value = price * item.quantity
            totalValue += value
            items.add(BankItemPayload(itemId = item.id, itemName = composition.name, quantity = item.quantity, value = value))
        }

        apiClient.enqueue(StatEvent.BankSnapshot(items = items, totalValue = totalValue))
    }
}
