package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.InventoryID
import net.runelite.api.Item
import net.runelite.api.ItemContainer
import net.runelite.client.game.ItemManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NetWorthTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var itemManager: ItemManager
    private lateinit var tracker: NetWorthTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        itemManager = mock()
        tracker = NetWorthTracker(apiClient, client, itemManager)
    }

    private fun container(vararg items: Item): ItemContainer {
        val container: ItemContainer = mock()
        whenever(container.items).thenReturn(arrayOf(*items))
        return container
    }

    @Test
    fun `sums inventory, equipment, and bank value by item price times quantity`() {
        val inventory = container(Item(995, 1000))
        val equipment = container(Item(1127, 1))
        val bank = container(Item(11840, 3))
        whenever(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(inventory)
        whenever(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(equipment)
        whenever(client.getItemContainer(InventoryID.BANK)).thenReturn(bank)
        whenever(itemManager.getItemPrice(995)).thenReturn(1)
        whenever(itemManager.getItemPrice(1127)).thenReturn(50000)
        whenever(itemManager.getItemPrice(11840)).thenReturn(2_000_000)

        tracker.snapshot()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.NetWorthSnapshot
        assertEquals(1000L, event.inventoryValue)
        assertEquals(50000L, event.equipmentValue)
        assertEquals(6_000_000L, event.bankValue)
        assertEquals(6_051_000L, event.totalValue)
    }

    @Test
    fun `treats a null container (never opened this session) as zero value`() {
        val inventory = container(Item(995, 500))
        whenever(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(inventory)
        whenever(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(null)
        whenever(client.getItemContainer(InventoryID.BANK)).thenReturn(null)
        whenever(itemManager.getItemPrice(995)).thenReturn(1)

        tracker.snapshot()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.NetWorthSnapshot
        assertEquals(0L, event.equipmentValue)
        assertEquals(0L, event.bankValue)
        assertEquals(500L, event.totalValue)
    }

    @Test
    fun `ignores empty item slots (id 0 or negative) instead of pricing them`() {
        val inventory = container(Item(-1, 0), Item(0, 0), Item(995, 10))
        whenever(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(inventory)
        whenever(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(null)
        whenever(client.getItemContainer(InventoryID.BANK)).thenReturn(null)
        whenever(itemManager.getItemPrice(995)).thenReturn(5)

        tracker.snapshot()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals(50L, (captor.firstValue as StatEvent.NetWorthSnapshot).inventoryValue)
    }

    @Test
    fun `does not emit when every container is empty or unavailable`() {
        whenever(client.getItemContainer(InventoryID.INVENTORY)).thenReturn(null)
        whenever(client.getItemContainer(InventoryID.EQUIPMENT)).thenReturn(null)
        whenever(client.getItemContainer(InventoryID.BANK)).thenReturn(null)

        tracker.snapshot()

        verify(apiClient, never()).enqueue(any())
    }
}
