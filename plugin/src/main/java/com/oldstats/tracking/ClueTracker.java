package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.util.Text;

/**
 * Tracks clue scroll completions via the completion chat message, using the
 * same regex RuneLite's own Loot Tracker plugin matches against
 * ({@code CLUE_SCROLL_PATTERN} in {@code LootTrackerPlugin}), extended with our own
 * capture group for the running total since the original only captures the
 * tier.
 */
public class ClueTracker {
    private static final Pattern CLUE_PATTERN =
        Pattern.compile("You have completed (\\d+) ([a-z]+) Treasure Trails?\\.");

    private final OldStatsApiClient apiClient;

    public ClueTracker(OldStatsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) return;
        String message = Text.removeTags(event.getMessage());

        Matcher matcher = CLUE_PATTERN.matcher(message);
        if (!matcher.find()) return;

        Integer count;
        try {
            count = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            count = null;
        }
        String tier = matcher.group(2);
        String capitalizedTier = Character.toUpperCase(tier.charAt(0)) + tier.substring(1);
        apiClient.enqueue(new StatEvent.ClueCompleted(capitalizedTier, count));
    }
}
