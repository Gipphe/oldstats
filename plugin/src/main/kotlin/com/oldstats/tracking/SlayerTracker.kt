package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.gameval.VarPlayerID
import net.runelite.api.gameval.VarbitID

/**
 * Tracks the current Slayer task the same way RuneLite's own built-in Slayer
 * plugin does — no chat parsing. The task's monster identity isn't stored as
 * plain text anywhere; it lives in Jagex's client-side DB table system,
 * looked up via [Client.getDBRowsByValue]/[Client.getDBTableField] keyed by
 * [VarPlayerID.SLAYER_TARGET] (DB table 113, field 10 = display name).
 * Wilderness/Krystilia tasks are a special case: `SLAYER_TARGET`
 * reads as the sentinel value 98, and the real creature id instead lives in
 * [VarbitID.SLAYER_TARGET_BOSSID], which cross-references into DB table 116
 * (field 4) to get the row id table 113 actually uses.
 *
 * [VarPlayerID.SLAYER_COUNT] is the remaining kill count; a task is
 * considered complete when it hits 0 before the next task is assigned (or,
 * failing that tick boundary, when the creature id changes while the
 * previous task's remaining count was already 0 — cancelling/skipping a task
 * before finishing it does not emit a completion).
 */
class SlayerTracker(
    private val apiClient: OldStatsApiClient,
    private val client: Client,
) {
    private var baselineEstablished = false
    private var currentCreature: Int? = null
    private var currentBossId: Int? = null
    private var currentTaskName: String? = null
    private var currentAmountAssigned: Int? = null
    private var lastAmountSeen: Int? = null
    private var startedAt: String? = null
    private var lastKnownPoints: Int? = null
    private var completionEmitted = false

    companion object {
        private const val WILDERNESS_SENTINEL = 98
        private const val TASK_TABLE = 113
        private const val WILDERNESS_TASK_TABLE = 116
        private const val TASK_NAME_FIELD = 10
    }

    fun reset() {
        baselineEstablished = false
        currentCreature = null
        currentBossId = null
        currentTaskName = null
        currentAmountAssigned = null
        lastAmountSeen = null
        startedAt = null
        lastKnownPoints = null
        completionEmitted = false
    }

    fun checkTask() {
        val creature = client.getVarpValue(VarPlayerID.SLAYER_TARGET)
        val bossId = if (creature == WILDERNESS_SENTINEL) client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID) else null
        val amount = client.getVarpValue(VarPlayerID.SLAYER_COUNT)
        val points = client.getVarbitValue(VarbitID.SLAYER_POINTS)

        if (!baselineEstablished) {
            baselineEstablished = true
            currentCreature = creature
            currentBossId = bossId
            lastAmountSeen = amount
            lastKnownPoints = points
            completionEmitted = false
            if (creature != 0) {
                currentTaskName = resolveTaskName(creature, bossId)
                currentAmountAssigned = amount
                startedAt = StatEvent.now()
            }
            return
        }

        val taskChanged = creature != currentCreature || (creature == WILDERNESS_SENTINEL && bossId != currentBossId)

        if (taskChanged) {
            if (currentCreature != null && currentCreature != 0 && lastAmountSeen == 0 && !completionEmitted) {
                emitCompletion(points)
            }
            currentCreature = creature
            currentBossId = bossId
            completionEmitted = false
            if (creature != 0) {
                currentTaskName = resolveTaskName(creature, bossId)
                currentAmountAssigned = amount
                startedAt = StatEvent.now()
            } else {
                currentTaskName = null
                currentAmountAssigned = null
                startedAt = null
            }
        } else if (creature != 0 && amount == 0 && (lastAmountSeen ?: 0) > 0 && !completionEmitted) {
            emitCompletion(points)
            completionEmitted = true
        }

        lastAmountSeen = amount
        lastKnownPoints = points
    }

    private fun emitCompletion(currentPoints: Int) {
        val name = currentTaskName ?: return
        val pointsEarned = lastKnownPoints?.let { (currentPoints - it).takeIf { delta -> delta > 0 } }
        apiClient.enqueue(
            StatEvent.SlayerTask(
                taskName = name,
                amountAssigned = currentAmountAssigned,
                points = pointsEarned,
                streak = null,
                startedAt = startedAt,
                completedAt = StatEvent.now(),
            )
        )
    }

    private fun resolveTaskName(creature: Int, bossId: Int?): String? {
        val rowId = if (creature == WILDERNESS_SENTINEL) {
            val id = bossId ?: return null
            val crossRefRow = client.getDBRowsByValue(WILDERNESS_TASK_TABLE, 1, 0, id).firstOrNull() ?: return null
            (client.getDBTableField(crossRefRow, 4, 0).getOrNull(0) as? Int) ?: return null
        } else {
            client.getDBRowsByValue(TASK_TABLE, 0, 0, creature).firstOrNull() ?: return null
        }
        return client.getDBTableField(rowId, TASK_NAME_FIELD, 0).getOrNull(0) as? String
    }
}
