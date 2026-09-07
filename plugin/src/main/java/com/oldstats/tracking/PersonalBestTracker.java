package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;

/**
 * Tracks new boss personal bests using the same regex RuneLite's own
 * {@code ChatCommandsPlugin} matches ({@code NEW_PB_PATTERN}) against the
 * "Fight duration: ... (new personal best)"-style message — which requires
 * the player to have the "Fight duration" chat setting enabled in-game, same
 * as the real plugin. That message never names the boss, so this attributes
 * it to whichever boss {@link LootTracker} most recently confirmed a kill for;
 * activities without a corresponding NPC kill (raids, agility laps, etc.)
 * aren't attributed and are skipped.
 */
public class PersonalBestTracker {
    private static final Pattern NEW_PB_PATTERN = Pattern.compile(
        "(?i)(?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?<!total )completion time:)"
            + " (?:<col=[0-9a-f]{6}>|@.+?@)([0-9:]+(?:\\.[0-9]+)?)</col> \\(new personal best\\)"
    );
    private static final Duration ATTRIBUTION_WINDOW = Duration.ofSeconds(10);

    private final OldStatsApiClient apiClient;
    private final LootTracker lootTracker;

    public PersonalBestTracker(OldStatsApiClient apiClient, LootTracker lootTracker) {
        this.apiClient = apiClient;
        this.lootTracker = lootTracker;
    }

    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) return;

        Matcher matcher = NEW_PB_PATTERN.matcher(event.getMessage());
        if (!matcher.find()) return;
        Double seconds = parseDuration(matcher.group(1));
        if (seconds == null) return;
        String boss = lootTracker.recentBossKill(ATTRIBUTION_WINDOW);
        if (boss == null) return;

        apiClient.enqueue(new StatEvent.PersonalBest(boss, seconds));
    }

    private Double parseDuration(String text) {
        String[] partsStr = text.split(":");
        double[] parts = new double[partsStr.length];
        for (int i = 0; i < partsStr.length; i++) {
            try {
                parts[i] = Double.parseDouble(partsStr[i]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        double total = 0.0;
        for (int index = 0; index < parts.length; index++) {
            int placeFromRight = parts.length - 1 - index;
            total += parts[index] * Math.pow(60.0, placeFromRight);
        }
        return total;
    }
}
