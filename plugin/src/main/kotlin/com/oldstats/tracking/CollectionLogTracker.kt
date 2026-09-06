package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.gameval.VarPlayerID
import net.runelite.client.game.ItemManager

/**
 * There is no dedicated "collection log item unlocked" event. Jagex's client
 * itself tracks the most recently unlocked item in a small ring buffer of
 * varps ([VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0] + its paired
 * `_DATE` varp is the newest), which is what drives the in-game "New item
 * added to your collection log" popup. Polling slot 0 on every VarbitChanged
 * and diffing against the last seen (itemId, date) pair reliably reports
 * every new unlock without needing the log interface to be open.
 */
class CollectionLogTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
    private val itemManager: ItemManager,
) {
    private var lastItemId: Int? = null
    private var lastDate: Int? = null

    fun reset() {
        lastItemId = null
        lastDate = null
    }

    fun checkNewestUnlock() {
        val itemId = client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0)
        val date = client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0_DATE)

        val previousItemId = lastItemId
        val previousDate = lastDate
        lastItemId = itemId
        lastDate = date

        if (itemId == 0 || date == 0) return
        if (previousItemId == null || previousDate == null) return // baseline only
        if (itemId == previousItemId && date == previousDate) return

        val totalUnlocked = client.getVarpValue(VarPlayerID.COLLECTION_COUNT)
        val totalPossible = client.getVarpValue(VarPlayerID.COLLECTION_COUNT_MAX)

        apiClient.enqueue(
            StatEvent.CollectionLogItem(
                itemName = itemManager.getItemComposition(itemId).name,
                itemId = itemId,
                totalUnlocked = totalUnlocked.takeIf { it > 0 },
                totalPossible = totalPossible.takeIf { it > 0 },
            )
        )
    }
}
