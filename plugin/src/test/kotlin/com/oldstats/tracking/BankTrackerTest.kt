package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.gameval.InventoryID
import net.runelite.api.Item
import net.runelite.api.ItemComposition
import net.runelite.api.ItemContainer
import net.runelite.api.events.ItemContainerChanged
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

class BankTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var itemManager: ItemManager
    private lateinit var tracker: BankTracker

    @Before
    fun setUp() {
        apiClient = mock()
        itemManager = mock()
        tracker = BankTracker(apiClient, itemManager)
    }

    private fun composition(name: String): ItemComposition {
        val comp: ItemComposition = mock()
        whenever(comp.name).thenReturn(name)
        return comp
    }

    private fun bankEvent(vararg items: Item, containerId: Int = InventoryID.BANK): ItemContainerChanged {
        val container: ItemContainer = mock()
        whenever(container.items).thenReturn(arrayOf(*items))
        whenever(container.size()).thenReturn(items.size)
        return ItemContainerChanged(containerId, container)
    }

    @Test
    fun `snapshots bank contents with resolved names and computed values`() {
        val coinsComp = composition("Coins")
        val clawsComp = composition("Dragon claws")
        whenever(itemManager.getItemComposition(995)).thenReturn(coinsComp)
        whenever(itemManager.getItemPrice(995)).thenReturn(1)
        whenever(itemManager.getItemComposition(11840)).thenReturn(clawsComp)
        whenever(itemManager.getItemPrice(11840)).thenReturn(2_000_000)

        tracker.onItemContainerChanged(bankEvent(Item(995, 1000), Item(11840, 1)))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.BankSnapshot
        assertEquals(2, event.items.size)
        assertEquals(2_001_000L, event.totalValue)
        val coins = event.items.first { it.itemId == 995 }
        assertEquals("Coins", coins.itemName)
        assertEquals(1000, coins.quantity)
        assertEquals(1000L, coins.value)
    }

    @Test
    fun `ignores changes to containers other than the bank`() {
        tracker.onItemContainerChanged(bankEvent(Item(995, 1000), containerId = InventoryID.INV))
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `skips empty slots (non-positive id or quantity)`() {
        val coins = composition("Coins")
        whenever(itemManager.getItemComposition(995)).thenReturn(coins)
        whenever(itemManager.getItemPrice(995)).thenReturn(1)

        tracker.onItemContainerChanged(bankEvent(Item(-1, 0), Item(0, 5), Item(995, 0), Item(995, 100)))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals(1, (captor.firstValue as StatEvent.BankSnapshot).items.size)
    }

    @Test
    fun `rate-limits back-to-back bank changes to avoid spamming a full snapshot per slot change`() {
        val coins = composition("Coins")
        whenever(itemManager.getItemComposition(995)).thenReturn(coins)
        whenever(itemManager.getItemPrice(995)).thenReturn(1)

        tracker.onItemContainerChanged(bankEvent(Item(995, 1000)))
        tracker.onItemContainerChanged(bankEvent(Item(995, 1001))) // fired immediately after, well under 10s

        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }

    @Test
    fun `an empty bank still emits a snapshot with zero total value`() {
        tracker.onItemContainerChanged(bankEvent())

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.BankSnapshot
        assertEquals(0, event.items.size)
        assertEquals(0L, event.totalValue)
    }
}
