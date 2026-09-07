package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

/**
 * Tracks individual achievement diary *task* completion — not just full-tier
 * completion, which {@link AchievementDiaryTracker} already covers via the
 * {@code <AREA>_DIARY_<TIER>_COMPLETE} varbits. There's no equivalent per-task
 * varbit; instead this mirrors what RuneLite's own (bundled) "Diary
 * Requirements" plugin does when annotating the diary journal with skill
 * requirements: the diary journal is rendered through the generic "journal
 * scroll" interface ({@link InterfaceID#JOURNALSCROLL}), whose already-completed
 * task lines are wrapped in {@code <str>} (strikethrough) tags by the game client
 * itself — confirmed by decompiling that plugin's {@code DiaryRequirementsPlugin},
 * which checks {@code text.startsWith("<str>")} for this exact reason.
 *
 * <p>Layout of that interface for a diary page (from the same source): the
 * title widget ({@link InterfaceID.Journalscroll#TITLE}) just reads "Achievement
 * Diary" generically; the actual per-area heading (e.g. "Ardougne Area
 * Tasks") is the *first* static child of the text layer
 * ({@link InterfaceID.Journalscroll#TEXTLAYER}), matched below against the exact
 * heading strings RuneLite's plugin switches on. Each area's page lists all
 * four tiers on one scroll with inline "Easy"/"Medium"/"Hard"/"Elite"
 * section headers ahead of that tier's task lines — this is standard,
 * well-known achievement diary UI layout, but wasn't independently
 * decompile-verified the way the heading strings and {@code <str>} marker were, so
 * treat the tier-grouping as best-effort.
 *
 * <p>Only sees a fresh snapshot when the player actually opens that area's
 * diary page in-game — same "can't be polled continuously" limitation as
 * {@link BankTracker}.
 */
public class DiaryTaskTracker {
    private static final Set<String> TIER_HEADERS = new HashSet<>(Set.of("EASY", "MEDIUM", "HARD", "ELITE"));

    // Verified against RuneLite's DiaryRequirementsPlugin#getRequirementsForTitle,
    // which switches on exactly these heading strings (title text with spaces
    // replaced by underscores and upper-cased) to pick each area's requirement set.
    private static final Map<String, String> AREA_HEADINGS = Map.ofEntries(
        Map.entry("ARDOUGNE_AREA_TASKS", "ARDOUGNE"),
        Map.entry("DESERT_TASKS", "DESERT"),
        Map.entry("FALADOR_AREA_TASKS", "FALADOR"),
        Map.entry("FREMENNIK_TASKS", "FREMENNIK"),
        Map.entry("KANDARIN_TASKS", "KANDARIN"),
        Map.entry("KARAMJA_AREA_TASKS", "KARAMJA"),
        Map.entry("KOUREND_&_KEBOS_TASKS", "KOUREND"),
        Map.entry("LUMBRIDGE_&_DRAYNOR_TASKS", "LUMBRIDGE"),
        Map.entry("MORYTANIA_TASKS", "MORYTANIA"),
        Map.entry("VARROCK_TASKS", "VARROCK"),
        Map.entry("WESTERN_AREA_TASKS", "WESTERN"),
        Map.entry("WILDERNESS_AREA_TASKS", "WILDERNESS")
    );

    private final OldStatsApiClient apiClient;
    private final Client client;
    private final ClientThread clientThread;

    public DiaryTaskTracker(OldStatsApiClient apiClient, Client client, ClientThread clientThread) {
        this.apiClient = apiClient;
        this.client = client;
        this.clientThread = clientThread;
    }

    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() != InterfaceID.JOURNALSCROLL) return;
        clientThread.invokeLater(this::readDiaryPage);
    }

    private void readDiaryPage() {
        Widget titleWidget = client.getWidget(InterfaceID.Journalscroll.TITLE);
        if (titleWidget == null || titleWidget.getText() == null) return;
        String title = Text.removeTags(titleWidget.getText()).replace(' ', '_').toUpperCase();
        if (!title.startsWith("ACHIEVEMENT_DIARY")) return;

        Widget textLayerWidget = client.getWidget(InterfaceID.Journalscroll.TEXTLAYER);
        if (textLayerWidget == null) return;
        Widget[] lines = textLayerWidget.getStaticChildren();
        if (lines == null || lines.length == 0) return;

        if (lines[0].getText() == null) return;
        String heading = Text.removeTags(lines[0].getText()).trim().replace(' ', '_').toUpperCase();
        String diaryArea = AREA_HEADINGS.get(heading);
        if (diaryArea == null) return;

        String currentTier = null;
        for (int i = 1; i < lines.length; i++) {
            String raw = lines[i].getText();
            if (raw == null) continue;
            String clean = Text.removeTags(raw).trim();
            if (clean.isEmpty()) continue;

            int spaceIdx = clean.indexOf(' ');
            String firstWord = (spaceIdx == -1 ? clean : clean.substring(0, spaceIdx)).toUpperCase();
            if (TIER_HEADERS.contains(firstWord) && clean.length() < 20) {
                currentTier = firstWord;
                continue;
            }

            if (currentTier == null) continue;
            apiClient.enqueue(
                new StatEvent.DiaryTaskProgress(diaryArea, currentTier, clean, raw.contains("<str>"))
            );
        }
    }
}
