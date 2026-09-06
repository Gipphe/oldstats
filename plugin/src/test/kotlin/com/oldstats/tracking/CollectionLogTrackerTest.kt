package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.ItemComposition
import net.runelite.api.gameval.VarPlayerID
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

/**
 * There is no dedicated collection-log-unlock event; [CollectionLogTracker]
 * diffs Jagex's own (itemId, date) ring-buffer slot 0 varps to detect a new
 * unlock, so these tests drive that pair directly rather than any UI event.
 */
class CollectionLogTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var itemManager: ItemManager
    private lateinit var tracker: CollectionLogTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        itemManager = mock()
        tracker = CollectionLogTracker(apiClient, client, itemManager)
    }

    private fun setSlot(itemId: Int, date: Int, unlocked: Int = 0, possible: Int = 0) {
        whenever(client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0)).thenReturn(itemId)
        whenever(client.getVarpValue(VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0_DATE)).thenReturn(date)
        whenever(client.getVarpValue(VarPlayerID.COLLECTION_COUNT)).thenReturn(unlocked)
        whenever(client.getVarpValue(VarPlayerID.COLLECTION_COUNT_MAX)).thenReturn(possible)
    }

    private fun composition(name: String): ItemComposition {
        val comp: ItemComposition = mock()
        whenever(comp.name).thenReturn(name)
        return comp
    }

    @Test
    fun `first check establishes a baseline without emitting, even with a nonzero slot`() {
        setSlot(itemId = 995, date = 111)
        tracker.checkNewestUnlock()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `emits when the ring buffer slot changes to a new item and date`() {
        setSlot(itemId = 995, date = 111)
        tracker.checkNewestUnlock() // baseline

        val comp = composition("Coins")
        whenever(itemManager.getItemComposition(20997)).thenReturn(comp)
        setSlot(itemId = 20997, date = 222, unlocked = 45, possible = 68)
        tracker.checkNewestUnlock()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.CollectionLogItem
        assertEquals("Coins", event.itemName)
        assertEquals(20997, event.itemId)
        assertEquals(45, event.totalUnlocked)
        assertEquals(68, event.totalPossible)
    }

    @Test
    fun `does not emit when the slot is unchanged`() {
        setSlot(itemId = 995, date = 111)
        tracker.checkNewestUnlock()
        setSlot(itemId = 995, date = 111)
        tracker.checkNewestUnlock()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `treats an empty ring buffer slot (id or date zero) as no unlock`() {
        setSlot(itemId = 0, date = 0)
        tracker.checkNewestUnlock()
        setSlot(itemId = 0, date = 0)
        tracker.checkNewestUnlock()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `reports null totals when the collection count varps read zero`() {
        setSlot(itemId = 995, date = 111)
        tracker.checkNewestUnlock() // baseline

        val comp = composition("Coins")
        whenever(itemManager.getItemComposition(20997)).thenReturn(comp)
        setSlot(itemId = 20997, date = 222, unlocked = 0, possible = 0)
        tracker.checkNewestUnlock()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.CollectionLogItem
        assertEquals(null, event.totalUnlocked)
        assertEquals(null, event.totalPossible)
    }

    @Test
    fun `reset re-establishes a fresh baseline`() {
        setSlot(itemId = 995, date = 111)
        tracker.checkNewestUnlock()

        val comp = composition("Coins")
        whenever(itemManager.getItemComposition(20997)).thenReturn(comp)
        setSlot(itemId = 20997, date = 222)
        tracker.checkNewestUnlock() // emits once

        tracker.reset()
        setSlot(itemId = 20997, date = 222)
        tracker.checkNewestUnlock() // re-baselines, no emission

        verify(apiClient, org.mockito.kotlin.times(1)).enqueue(any())
    }
}
