package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class BankTrackerTest {
    private OldStatsApiClient apiClient;
    private ItemManager itemManager;
    private BankTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        itemManager = mock(ItemManager.class);
        tracker = new BankTracker(apiClient, itemManager);
    }

    private ItemComposition composition(String name) {
        ItemComposition comp = mock(ItemComposition.class);
        when(comp.getName()).thenReturn(name);
        return comp;
    }

    private ItemContainerChanged bankEvent(Item... items) {
        return bankEvent(InventoryID.BANK, items);
    }

    private ItemContainerChanged bankEvent(int containerId, Item... items) {
        ItemContainer container = mock(ItemContainer.class);
        when(container.getItems()).thenReturn(items);
        when(container.size()).thenReturn(items.length);
        return new ItemContainerChanged(containerId, container);
    }

    @Test
    public void snapshotsBankContentsWithResolvedNamesAndComputedValues() {
        ItemComposition coinsComp = composition("Coins");
        ItemComposition clawsComp = composition("Dragon claws");
        when(itemManager.getItemComposition(995)).thenReturn(coinsComp);
        when(itemManager.getItemPrice(995)).thenReturn(1);
        when(itemManager.getItemComposition(11840)).thenReturn(clawsComp);
        when(itemManager.getItemPrice(11840)).thenReturn(2_000_000);

        tracker.onItemContainerChanged(bankEvent(new Item(995, 1000), new Item(11840, 1)));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.BankSnapshot event = (StatEvent.BankSnapshot) captor.getValue();
        assertEquals(2, event.items.size());
        assertEquals(2_001_000L, event.totalValue);
        var coins = event.items.stream().filter(it -> it.itemId == 995).findFirst().orElseThrow();
        assertEquals("Coins", coins.itemName);
        assertEquals(1000, coins.quantity);
        assertEquals(1000L, coins.value);
    }

    @Test
    public void ignoresChangesToContainersOtherThanTheBank() {
        tracker.onItemContainerChanged(bankEvent(InventoryID.INV, new Item(995, 1000)));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void skipsEmptySlotsNonPositiveIdOrQuantity() {
        ItemComposition coins = composition("Coins");
        when(itemManager.getItemComposition(995)).thenReturn(coins);
        when(itemManager.getItemPrice(995)).thenReturn(1);

        tracker.onItemContainerChanged(
            bankEvent(new Item(-1, 0), new Item(0, 5), new Item(995, 0), new Item(995, 100))
        );

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals(1, ((StatEvent.BankSnapshot) captor.getValue()).items.size());
    }

    @Test
    public void rateLimitsBackToBackBankChangesToAvoidSpammingAFullSnapshotPerSlotChange() {
        ItemComposition coins = composition("Coins");
        when(itemManager.getItemComposition(995)).thenReturn(coins);
        when(itemManager.getItemPrice(995)).thenReturn(1);

        tracker.onItemContainerChanged(bankEvent(new Item(995, 1000)));
        tracker.onItemContainerChanged(bankEvent(new Item(995, 1001))); // fired immediately after, well under 10s

        verify(apiClient, times(1)).enqueue(any());
    }

    @Test
    public void anEmptyBankStillEmitsASnapshotWithZeroTotalValue() {
        tracker.onItemContainerChanged(bankEvent());

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.BankSnapshot event = (StatEvent.BankSnapshot) captor.getValue();
        assertEquals(0, event.items.size());
        assertEquals(0L, event.totalValue);
    }
}
