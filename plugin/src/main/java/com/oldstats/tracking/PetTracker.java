package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.List;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;

/**
 * There's no dedicated "pet received" event, and the chat message doesn't
 * name the pet. This watches for one of the three known pet-drop messages,
 * then attributes it to whichever NPC spawns next to the player within a
 * few ticks (pets that auto-follow spawn this way). Pets received directly
 * to the inventory instead (follower slot already occupied, or no space)
 * won't have a name resolved and are reported as "Unknown pet".
 */
public class PetTracker {
    private static final int PET_WAIT_TICKS = 3;
    private static final List<String> PET_MESSAGES = List.of(
        "you have a funny feeling like you're being followed",
        "you feel something weird sneaking into your backpack"
    );

    private final OldStatsApiClient apiClient;
    private final Client client;

    private Integer pendingSince;
    private int tick = 0;

    public PetTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
    }

    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) return;
        String message = event.getMessage().toLowerCase();
        if (PET_MESSAGES.stream().anyMatch(message::contains)) {
            pendingSince = tick;
        }
    }

    public void onNpcSpawned(NpcSpawned event) {
        Integer since = pendingSince;
        if (since == null) return;
        if (tick - since > PET_WAIT_TICKS) {
            pendingSince = null;
            apiClient.enqueue(new StatEvent.PetReceived(null));
            return;
        }
        NPC npc = event.getNpc();
        Player localPlayer = client.getLocalPlayer();
        WorldPoint playerLoc = localPlayer != null ? localPlayer.getWorldLocation() : null;
        if (playerLoc == null) return;
        WorldPoint npcLoc = npc.getWorldLocation();
        if (npcLoc == null) return;
        if (npcLoc.distanceTo(playerLoc) <= 1) {
            pendingSince = null;
            apiClient.enqueue(new StatEvent.PetReceived(npc.getName()));
        }
    }

    public void onGameTick(GameTick event) {
        tick++;
        Integer since = pendingSince;
        if (since == null) return;
        if (tick - since > PET_WAIT_TICKS) {
            pendingSince = null;
            apiClient.enqueue(new StatEvent.PetReceived(null));
        }
    }
}
