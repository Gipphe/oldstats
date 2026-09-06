package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.gameval.VarbitID
import java.util.concurrent.ConcurrentHashMap

/**
 * There is no dedicated "combat achievement completed" event or a `Quest`-like
 * enum to enumerate tasks by. RuneLite's generated [VarbitID] constants do
 * expose one boolean varbit per task named `CA_TASK_<...>_COMPLETED`
 * (~400 of them) plus a single [VarbitID.CA_POINTS] running total, so this
 * reflects over those field names once at startup and polls/diffs them on
 * every VarbitChanged tick — the same pattern as [QuestTracker]. The derived
 * task name (e.g. "Vorkath Killcount 1") comes from the varbit's own
 * constant name, not Jagex's actual task title, since that mapping isn't
 * exposed anywhere in the client API.
 */
class CombatAchievementTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private val taskVarbits: List<Pair<String, Int>> by lazy {
        VarbitID::class.java.fields
            .filter { it.name.startsWith("CA_TASK_") && it.name.endsWith("_COMPLETED") }
            .map { field ->
                val middle = field.name.removePrefix("CA_TASK_").removeSuffix("_COMPLETED")
                titleCase(middle) to field.getInt(null)
            }
    }

    private val lastValues = ConcurrentHashMap<Int, Int>()

    fun reset() {
        lastValues.clear()
    }

    fun checkTasks() {
        for ((name, varbitId) in taskVarbits) {
            val value = client.getVarbitValue(varbitId)
            val previous = lastValues.put(varbitId, value)
            if (previous == null) continue // baseline only
            if (previous == 0 && value != 0) {
                val totalPoints = client.getVarbitValue(VarbitID.CA_POINTS)
                apiClient.enqueue(
                    StatEvent.CombatAchievement(taskName = name, totalPoints = totalPoints.takeIf { it > 0 })
                )
            }
        }
    }

    private fun titleCase(name: String): String =
        name.lowercase().split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
}
