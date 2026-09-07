package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class LootTrackerTest {
    private OldStatsApiClient apiClient;
    private ItemManager itemManager;
    private LootTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        itemManager = mock(ItemManager.class);
        tracker = new LootTracker(apiClient, itemManager);
    }

    private NPC npc(String name, int id) {
        NPC n = mock(NPC.class);
        when(n.getName()).thenReturn(name);
        when(n.getId()).thenReturn(id);
        return n;
    }

    private void stubItem(int itemId, String name, int price) {
        ItemComposition composition = mock(ItemComposition.class);
        when(composition.getName()).thenReturn(name);
        when(itemManager.getItemComposition(itemId)).thenReturn(composition);
        when(itemManager.getItemPrice(itemId)).thenReturn(price);
    }

    @Test
    public void emitsAKillAndADropForEveryItemInTheLoot() {
        stubItem(21295, "Draconic visage", 8_500_000);
        tracker.onNpcLootReceived(new NpcLootReceived(npc("Vorkath", 8058), List.of(new ItemStack(21295, 1))));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(2)).enqueue(captor.capture());

        StatEvent.Kill kill = (StatEvent.Kill) captor.getAllValues().get(0);
        assertEquals("Vorkath", kill.npcName);
        assertEquals(Integer.valueOf(8058), kill.npcId);
        assertTrue(kill.isBoss);

        StatEvent.Drop drop = (StatEvent.Drop) captor.getAllValues().get(1);
        assertEquals("Draconic visage", drop.itemName);
        assertEquals(Integer.valueOf(21295), drop.itemId);
        assertEquals(1, drop.quantity);
        assertEquals(8_500_000L, drop.value);
    }

    @Test
    public void multipliesUnitPriceByQuantityForStackedDrops() {
        stubItem(12934, "Zulrah's scales", 20);
        tracker.onNpcLootReceived(new NpcLootReceived(npc("Zulrah", 2042), List.of(new ItemStack(12934, 1000))));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(2)).enqueue(captor.capture());
        StatEvent.Drop drop = (StatEvent.Drop) captor.getAllValues().get(1);
        assertEquals(20_000L, drop.value);
    }

    @Test
    public void flagsANonBossKillCorrectlyAndEmitsNoDropsWhenTheresNoLoot() {
        tracker.onNpcLootReceived(new NpcLootReceived(npc("Chicken", 41), Collections.emptyList()));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.Kill kill = (StatEvent.Kill) captor.getValue();
        assertEquals("Chicken", kill.npcName);
        assertTrue(!kill.isBoss);
    }

    @Test
    public void fallsBackToUnknownWhenTheNpcHasNoName() {
        NPC unnamed = mock(NPC.class);
        when(unnamed.getId()).thenReturn(1);
        tracker.onNpcLootReceived(new NpcLootReceived(unnamed, Collections.emptyList()));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("Unknown", ((StatEvent.Kill) captor.getValue()).npcName);
    }

    @Test
    public void recentBossKillRemembersOnlyTheMostRecentBossWithinTheGivenWindow() {
        assertNull(tracker.recentBossKill(Duration.ofSeconds(10)));

        tracker.onNpcLootReceived(new NpcLootReceived(npc("Vorkath", 8058), Collections.emptyList()));
        assertEquals("Vorkath", tracker.recentBossKill(Duration.ofSeconds(10)));

        // A non-boss kill shouldn't clear or override the last boss kill.
        tracker.onNpcLootReceived(new NpcLootReceived(npc("Chicken", 41), Collections.emptyList()));
        assertEquals("Vorkath", tracker.recentBossKill(Duration.ofSeconds(10)));
    }

    @Test
    public void recentBossKillReturnsNullOnceTheWindowHasElapsed() {
        tracker.onNpcLootReceived(new NpcLootReceived(npc("Vorkath", 8058), Collections.emptyList()));
        assertNull(tracker.recentBossKill(Duration.ZERO.minusNanos(1)));
    }
}
