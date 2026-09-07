package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.game.ItemManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * There is no dedicated collection-log-unlock event; {@link CollectionLogTracker}
 * diffs Jagex's own (itemId, date) ring-buffer slot 0 varps to detect a new
 * unlock, so these tests drive that pair directly rather than any UI event.
 */
public class CollectionLogTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private ItemManager itemManager;
    private CollectionLogTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        itemManager = mock(ItemManager.class);
        tracker = new CollectionLogTracker(apiClient, client, itemManager);
    }

    private void setSlot(int itemId, int date) {
        setSlot(itemId, date, 0, 0);
    }

    private void setSlot(int itemId, int date, int unlocked, int possible) {
        when(client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0)).thenReturn(itemId);
        when(client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0_DATE)).thenReturn(date);
        when(client.getVarpValue(VarPlayerID.COLLECTION_COUNT)).thenReturn(unlocked);
        when(client.getVarpValue(VarPlayerID.COLLECTION_COUNT_MAX)).thenReturn(possible);
    }

    private ItemComposition composition(String name) {
        ItemComposition comp = mock(ItemComposition.class);
        when(comp.getName()).thenReturn(name);
        return comp;
    }

    @Test
    public void firstCheckEstablishesABaselineWithoutEmittingEvenWithANonzeroSlot() {
        setSlot(995, 111);
        tracker.checkNewestUnlock();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void emitsWhenTheRingBufferSlotChangesToANewItemAndDate() {
        setSlot(995, 111);
        tracker.checkNewestUnlock(); // baseline

        ItemComposition comp = composition("Coins");
        when(itemManager.getItemComposition(20997)).thenReturn(comp);
        setSlot(20997, 222, 45, 68);
        tracker.checkNewestUnlock();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.CollectionLogItem event = (StatEvent.CollectionLogItem) captor.getValue();
        assertEquals("Coins", event.itemName);
        assertEquals(Integer.valueOf(20997), event.itemId);
        assertEquals(Integer.valueOf(45), event.totalUnlocked);
        assertEquals(Integer.valueOf(68), event.totalPossible);
    }

    @Test
    public void doesNotEmitWhenTheSlotIsUnchanged() {
        setSlot(995, 111);
        tracker.checkNewestUnlock();
        setSlot(995, 111);
        tracker.checkNewestUnlock();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void treatsAnEmptyRingBufferSlotIdOrDateZeroAsNoUnlock() {
        setSlot(0, 0);
        tracker.checkNewestUnlock();
        setSlot(0, 0);
        tracker.checkNewestUnlock();
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void reportsNullTotalsWhenTheCollectionCountVarpsReadZero() {
        setSlot(995, 111);
        tracker.checkNewestUnlock(); // baseline

        ItemComposition comp = composition("Coins");
        when(itemManager.getItemComposition(20997)).thenReturn(comp);
        setSlot(20997, 222, 0, 0);
        tracker.checkNewestUnlock();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.CollectionLogItem event = (StatEvent.CollectionLogItem) captor.getValue();
        assertNull(event.totalUnlocked);
        assertNull(event.totalPossible);
    }

    @Test
    public void resetReEstablishesAFreshBaseline() {
        setSlot(995, 111);
        tracker.checkNewestUnlock();

        ItemComposition comp = composition("Coins");
        when(itemManager.getItemComposition(20997)).thenReturn(comp);
        setSlot(20997, 222);
        tracker.checkNewestUnlock(); // emits once

        tracker.reset();
        setSlot(20997, 222);
        tracker.checkNewestUnlock(); // re-baselines, no emission

        verify(apiClient, times(1)).enqueue(any());
    }
}
