package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.events.WidgetLoaded
import net.runelite.api.gameval.InterfaceID
import net.runelite.client.callback.ClientThread
import net.runelite.client.util.Text

/**
 * Tracks individual achievement diary *task* completion — not just full-tier
 * completion, which [AchievementDiaryTracker] already covers via the
 * `<AREA>_DIARY_<TIER>_COMPLETE` varbits. There's no equivalent per-task
 * varbit; instead this mirrors what RuneLite's own (bundled) "Diary
 * Requirements" plugin does when annotating the diary journal with skill
 * requirements: the diary journal is rendered through the generic "journal
 * scroll" interface ([InterfaceID.JOURNALSCROLL]), whose already-completed
 * task lines are wrapped in `<str>` (strikethrough) tags by the game client
 * itself — confirmed by decompiling that plugin's `DiaryRequirementsPlugin`,
 * which checks `text.startsWith("<str>")` for this exact reason.
 *
 * Layout of that interface for a diary page (from the same source): the
 * title widget ([InterfaceID.Journalscroll.TITLE]) just reads "Achievement
 * Diary" generically; the actual per-area heading (e.g. "Ardougne Area
 * Tasks") is the *first* static child of the text layer
 * ([InterfaceID.Journalscroll.TEXTLAYER]), matched below against the exact
 * heading strings RuneLite's plugin switches on. Each area's page lists all
 * four tiers on one scroll with inline "Easy"/"Medium"/"Hard"/"Elite"
 * section headers ahead of that tier's task lines — this is standard,
 * well-known achievement diary UI layout, but wasn't independently
 * decompile-verified the way the heading strings and `<str>` marker were, so
 * treat the tier-grouping as best-effort.
 *
 * Only sees a fresh snapshot when the player actually opens that area's
 * diary page in-game — same "can't be polled continuously" limitation as
 * [BankTracker].
 */
class DiaryTaskTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
    private val clientThread: ClientThread,
) {
    companion object {
        private val TIER_HEADERS = setOf("EASY", "MEDIUM", "HARD", "ELITE")

        // Verified against RuneLite's DiaryRequirementsPlugin#getRequirementsForTitle,
        // which switches on exactly these heading strings (title text with spaces
        // replaced by underscores and upper-cased) to pick each area's requirement set.
        private val AREA_HEADINGS = mapOf(
            "ARDOUGNE_AREA_TASKS" to "ARDOUGNE",
            "DESERT_TASKS" to "DESERT",
            "FALADOR_AREA_TASKS" to "FALADOR",
            "FREMENNIK_TASKS" to "FREMENNIK",
            "KANDARIN_TASKS" to "KANDARIN",
            "KARAMJA_AREA_TASKS" to "KARAMJA",
            "KOUREND_&_KEBOS_TASKS" to "KOUREND",
            "LUMBRIDGE_&_DRAYNOR_TASKS" to "LUMBRIDGE",
            "MORYTANIA_TASKS" to "MORYTANIA",
            "VARROCK_TASKS" to "VARROCK",
            "WESTERN_AREA_TASKS" to "WESTERN",
            "WILDERNESS_AREA_TASKS" to "WILDERNESS",
        )
    }

    fun onWidgetLoaded(event: WidgetLoaded) {
        if (event.groupId != InterfaceID.JOURNALSCROLL) return
        clientThread.invokeLater(Runnable { readDiaryPage() })
    }

    private fun readDiaryPage() {
        val titleWidget = client.getWidget(InterfaceID.Journalscroll.TITLE) ?: return
        val title = Text.removeTags(titleWidget.text ?: return).replace(' ', '_').uppercase()
        if (!title.startsWith("ACHIEVEMENT_DIARY")) return

        val lines = client.getWidget(InterfaceID.Journalscroll.TEXTLAYER)?.staticChildren ?: return
        if (lines.isEmpty()) return

        val heading = Text.removeTags(lines[0].text ?: return).trim().replace(' ', '_').uppercase()
        val diaryArea = AREA_HEADINGS[heading] ?: return

        var currentTier: String? = null
        for (i in 1 until lines.size) {
            val raw = lines[i].text ?: continue
            val clean = Text.removeTags(raw).trim()
            if (clean.isEmpty()) continue

            val firstWord = clean.substringBefore(' ').uppercase()
            if (firstWord in TIER_HEADERS && clean.length < 20) {
                currentTier = firstWord
                continue
            }

            val tier = currentTier ?: continue
            apiClient.enqueue(
                StatEvent.DiaryTaskProgress(
                    diaryArea = diaryArea,
                    tier = tier,
                    taskName = clean,
                    completed = raw.contains("<str>"),
                )
            )
        }
    }
}
