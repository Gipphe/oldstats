package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.ItemComposition
import net.runelite.api.NPC
import net.runelite.client.events.NpcLootReceived
import net.runelite.client.game.ItemManager
import net.runelite.client.game.ItemStack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LootTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var itemManager: ItemManager
    private lateinit var tracker: LootTracker

    @Before
    fun setUp() {
        apiClient = mock()
        itemManager = mock()
        tracker = LootTracker(apiClient, itemManager)
    }

    private fun npc(name: String, id: Int): NPC {
        val n: NPC = mock()
        whenever(n.name).thenReturn(name)
        whenever(n.id).thenReturn(id)
        return n
    }

    private fun stubItem(itemId: Int, name: String, price: Int) {
        val composition: ItemComposition = mock()
        whenever(composition.name).thenReturn(name)
        whenever(itemManager.getItemComposition(itemId)).thenReturn(composition)
        whenever(itemManager.getItemPrice(itemId)).thenReturn(price)
    }

    @Test
    fun `emits a kill and a drop for every item in the loot`() {
        stubItem(21295, "Draconic visage", 8_500_000)
        tracker.onNpcLootReceived(NpcLootReceived(npc("Vorkath", 8058), listOf(ItemStack(21295, 1))))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient, org.mockito.kotlin.times(2)).enqueue(captor.capture())

        val kill = captor.allValues[0] as StatEvent.Kill
        assertEquals("Vorkath", kill.npcName)
        assertEquals(8058, kill.npcId)
        assertTrue(kill.isBoss)

        val drop = captor.allValues[1] as StatEvent.Drop
        assertEquals("Draconic visage", drop.itemName)
        assertEquals(21295, drop.itemId)
        assertEquals(1, drop.quantity)
        assertEquals(8_500_000L, drop.value)
    }

    @Test
    fun `multiplies unit price by quantity for stacked drops`() {
        stubItem(12934, "Zulrah's scales", 20)
        tracker.onNpcLootReceived(NpcLootReceived(npc("Zulrah", 2042), listOf(ItemStack(12934, 1000))))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient, org.mockito.kotlin.times(2)).enqueue(captor.capture())
        val drop = captor.allValues[1] as StatEvent.Drop
        assertEquals(20_000L, drop.value)
    }

    @Test
    fun `flags a non-boss kill correctly and emits no drops when there's no loot`() {
        tracker.onNpcLootReceived(NpcLootReceived(npc("Chicken", 41), emptyList()))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val kill = captor.firstValue as StatEvent.Kill
        assertEquals("Chicken", kill.npcName)
        assertTrue(!kill.isBoss)
    }

    @Test
    fun `falls back to Unknown when the npc has no name`() {
        val unnamed: NPC = mock()
        whenever(unnamed.id).thenReturn(1)
        tracker.onNpcLootReceived(NpcLootReceived(unnamed, emptyList()))

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("Unknown", (captor.firstValue as StatEvent.Kill).npcName)
    }

    @Test
    fun `recentBossKill remembers only the most recent boss, within the given window`() {
        assertNull(tracker.recentBossKill(Duration.ofSeconds(10)))

        tracker.onNpcLootReceived(NpcLootReceived(npc("Vorkath", 8058), emptyList()))
        assertEquals("Vorkath", tracker.recentBossKill(Duration.ofSeconds(10)))

        // A non-boss kill shouldn't clear or override the last boss kill.
        tracker.onNpcLootReceived(NpcLootReceived(npc("Chicken", 41), emptyList()))
        assertEquals("Vorkath", tracker.recentBossKill(Duration.ofSeconds(10)))
    }

    @Test
    fun `recentBossKill returns null once the window has elapsed`() {
        tracker.onNpcLootReceived(NpcLootReceived(npc("Vorkath", 8058), emptyList()))
        assertNull(tracker.recentBossKill(Duration.ZERO.minusNanos(1)))
    }
}
