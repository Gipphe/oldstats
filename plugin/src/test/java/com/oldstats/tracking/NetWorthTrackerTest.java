package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class NetWorthTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private ItemManager itemManager;
    private NetWorthTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        itemManager = mock(ItemManager.class);
        tracker = new NetWorthTracker(apiClient, client, itemManager);
    }

    private ItemContainer container(Item... items) {
        ItemContainer container = mock(ItemContainer.class);
        when(container.getItems()).thenReturn(items);
        return container;
    }

    @Test
    public void sumsInventoryEquipmentAndBankValueByItemPriceTimesQuantity() {
        ItemContainer inventory = container(new Item(995, 1000));
        ItemContainer equipment = container(new Item(1127, 1));
        ItemContainer bank = container(new Item(11840, 3));
        when(client.getItemContainer(InventoryID.INV)).thenReturn(inventory);
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(equipment);
        when(client.getItemContainer(InventoryID.BANK)).thenReturn(bank);
        when(itemManager.getItemPrice(995)).thenReturn(1);
        when(itemManager.getItemPrice(1127)).thenReturn(50000);
        when(itemManager.getItemPrice(11840)).thenReturn(2_000_000);

        tracker.snapshot();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.NetWorthSnapshot event = (StatEvent.NetWorthSnapshot) captor.getValue();
        assertEquals(1000L, event.inventoryValue);
        assertEquals(50000L, event.equipmentValue);
        assertEquals(6_000_000L, event.bankValue);
        assertEquals(6_051_000L, event.totalValue);
    }

    @Test
    public void treatsANullContainerNeverOpenedThisSessionAsZeroValue() {
        ItemContainer inventory = container(new Item(995, 500));
        when(client.getItemContainer(InventoryID.INV)).thenReturn(inventory);
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(null);
        when(client.getItemContainer(InventoryID.BANK)).thenReturn(null);
        when(itemManager.getItemPrice(995)).thenReturn(1);

        tracker.snapshot();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.NetWorthSnapshot event = (StatEvent.NetWorthSnapshot) captor.getValue();
        assertEquals(0L, event.equipmentValue);
        assertEquals(0L, event.bankValue);
        assertEquals(500L, event.totalValue);
    }

    @Test
    public void ignoresEmptyItemSlotsIdZeroOrNegativeInsteadOfPricingThem() {
        ItemContainer inventory = container(new Item(-1, 0), new Item(0, 0), new Item(995, 10));
        when(client.getItemContainer(InventoryID.INV)).thenReturn(inventory);
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(null);
        when(client.getItemContainer(InventoryID.BANK)).thenReturn(null);
        when(itemManager.getItemPrice(995)).thenReturn(5);

        tracker.snapshot();

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals(50L, ((StatEvent.NetWorthSnapshot) captor.getValue()).inventoryValue);
    }

    @Test
    public void doesNotEmitWhenEveryContainerIsEmptyOrUnavailable() {
        when(client.getItemContainer(InventoryID.INV)).thenReturn(null);
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(null);
        when(client.getItemContainer(InventoryID.BANK)).thenReturn(null);

        tracker.snapshot();

        verify(apiClient, never()).enqueue(any());
    }
}
