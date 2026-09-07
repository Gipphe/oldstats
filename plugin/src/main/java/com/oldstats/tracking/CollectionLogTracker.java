package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.game.ItemManager;

/**
 * There is no dedicated "collection log item unlocked" event. Jagex's client
 * itself tracks the most recently unlocked item in a small ring buffer of
 * varps ({@link VarPlayerID#COLLECTION_OVERVIEW_LAST_ITEM0} + its paired
 * {@code _DATE} varp is the newest), which is what drives the in-game "New
 * item added to your collection log" popup. Polling slot 0 on every
 * VarbitChanged and diffing against the last seen (itemId, date) pair
 * reliably reports every new unlock without needing the log interface to be
 * open.
 */
public class CollectionLogTracker {
    private final OldStatsApiClient apiClient;
    private final Client client;
    private final ItemManager itemManager;

    private Integer lastItemId;
    private Integer lastDate;

    public CollectionLogTracker(OldStatsApiClient apiClient, Client client, ItemManager itemManager) {
        this.apiClient = apiClient;
        this.client = client;
        this.itemManager = itemManager;
    }

    public void reset() {
        lastItemId = null;
        lastDate = null;
    }

    public void checkNewestUnlock() {
        int itemId = client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0);
        int date = client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0_DATE);

        Integer previousItemId = lastItemId;
        Integer previousDate = lastDate;
        lastItemId = itemId;
        lastDate = date;

        if (itemId == 0 || date == 0) return;
        if (previousItemId == null || previousDate == null) return; // baseline only
        if (itemId == previousItemId && date == previousDate) return;

        int totalUnlocked = client.getVarpValue(VarPlayerID.COLLECTION_COUNT);
        int totalPossible = client.getVarpValue(VarPlayerID.COLLECTION_COUNT_MAX);

        apiClient.enqueue(
            new StatEvent.CollectionLogItem(
                itemManager.getItemComposition(itemId).getName(),
                itemId,
                totalUnlocked > 0 ? totalUnlocked : null,
                totalPossible > 0 ? totalPossible : null
            )
        );
    }
}
