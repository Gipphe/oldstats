package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PetTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private PetTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        tracker = new PetTracker(apiClient, client);

        Player localPlayer = mock(Player.class);
        when(localPlayer.getWorldLocation()).thenReturn(new WorldPoint(100, 100, 0));
        when(client.getLocalPlayer()).thenReturn(localPlayer);
    }

    private ChatMessage chatMessage(String text) {
        return new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", text, "", 0);
    }

    private NPC npcAt(String name, WorldPoint point) {
        NPC npc = mock(NPC.class);
        when(npc.getName()).thenReturn(name);
        when(npc.getWorldLocation()).thenReturn(point);
        return npc;
    }

    @Test
    public void attributesThePetToAnNpcSpawningRightNextToThePlayer() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you're being followed."));
        tracker.onNpcSpawned(new NpcSpawned(npcAt("Vorki", new WorldPoint(100, 101, 0))));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("Vorki", ((StatEvent.PetReceived) captor.getValue()).petName);
    }

    @Test
    public void recognizesTheAlternateSneakingIntoYourBackpackMessage() {
        tracker.onChatMessage(chatMessage("You feel something weird sneaking into your backpack."));
        tracker.onNpcSpawned(new NpcSpawned(npcAt("Baby chinchompa", new WorldPoint(100, 100, 0))));

        verify(apiClient).enqueue(any(StatEvent.PetReceived.class));
    }

    @Test
    public void doesNotTreatTheNearMissWouldHaveBeenFollowedMessageAsAPet() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you would have been followed..."));
        tracker.onNpcSpawned(new NpcSpawned(npcAt("Random npc", new WorldPoint(100, 100, 0))));

        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void ignoresAnNpcSpawningFarAwayFromThePlayer() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you're being followed."));
        tracker.onNpcSpawned(new NpcSpawned(npcAt("Random npc", new WorldPoint(500, 500, 0))));

        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void timesOutToAnUnknownPetAfterTooManyTicksPassWithNoMatchingSpawn() {
        tracker.onChatMessage(chatMessage("You have a funny feeling like you're being followed."));
        for (int i = 0; i < 4; i++) {
            tracker.onGameTick(new GameTick());
        }

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertNull(((StatEvent.PetReceived) captor.getValue()).petName);
    }

    @Test
    public void anNpcSpawnWithNoPrecedingPetMessageIsIgnored() {
        tracker.onNpcSpawned(new NpcSpawned(npcAt("Random npc", new WorldPoint(100, 100, 0))));
        verify(apiClient, never()).enqueue(any());
    }
}
