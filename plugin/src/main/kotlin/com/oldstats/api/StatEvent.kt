package com.oldstats.api

import java.time.Instant

data class BankItemPayload(
    val itemId: Int?,
    val itemName: String,
    val quantity: Int,
    val value: Long,
)

/**
 * Mirrors the discriminated union accepted by the OldStats server's
 * POST /api/events endpoint (server/src/types/events.ts). Field names must
 * match exactly since Gson serializes by declared property name.
 */
sealed class StatEvent(val type: String) {

    class XpGain(
        val skill: String,
        val xp: Long,
        val xpGained: Long,
        val level: Int,
        val ts: String = now(),
    ) : StatEvent("xp_gain")

    class LevelUp(
        val skill: String,
        val level: Int,
        val ts: String = now(),
    ) : StatEvent("level_up")

    class Kill(
        val npcName: String,
        val npcId: Int?,
        val isBoss: Boolean,
        val ts: String = now(),
    ) : StatEvent("kill")

    class Drop(
        val npcName: String?,
        val itemName: String,
        val itemId: Int?,
        val quantity: Int,
        val value: Long,
        val ts: String = now(),
    ) : StatEvent("drop")

    class Quest(
        val questName: String,
        val state: String,
        val questPoints: Int?,
        val ts: String = now(),
    ) : StatEvent("quest")

    class SlayerTask(
        val taskName: String,
        val amountAssigned: Int?,
        val points: Int?,
        val streak: Int?,
        val startedAt: String?,
        val completedAt: String?,
    ) : StatEvent("slayer_task")

    class FarmingPatch(
        val patchName: String,
        val crop: String?,
        val state: String,
        val ts: String = now(),
    ) : StatEvent("farming_patch")

    class CollectionLogItem(
        val itemName: String,
        val itemId: Int?,
        val totalUnlocked: Int?,
        val totalPossible: Int?,
        val ts: String = now(),
    ) : StatEvent("collection_log_item")

    class CombatAchievement(
        val taskName: String,
        val totalPoints: Int?,
        val ts: String = now(),
    ) : StatEvent("combat_achievement")

    class DiaryCompleted(
        val diaryArea: String,
        val tier: String,
        val ts: String = now(),
    ) : StatEvent("diary_completed")

    class ClueCompleted(
        val tier: String,
        val count: Int?,
        val ts: String = now(),
    ) : StatEvent("clue_completed")

    class PetReceived(
        val petName: String?,
        val ts: String = now(),
    ) : StatEvent("pet_received")

    class PersonalBest(
        val activityName: String,
        val durationSeconds: Double,
        val ts: String = now(),
    ) : StatEvent("personal_best")

    class PlayerKill(
        val opponentName: String,
        val ts: String = now(),
    ) : StatEvent("player_kill")

    class PlayerDeath(
        val valueLost: Long?,
        val ts: String = now(),
    ) : StatEvent("player_death")

    class WorldChange(
        val world: Int,
        val worldTypes: List<String>,
        val ts: String = now(),
    ) : StatEvent("world_change")

    class NetWorthSnapshot(
        val inventoryValue: Long,
        val equipmentValue: Long,
        val bankValue: Long,
        val totalValue: Long,
        val ts: String = now(),
    ) : StatEvent("net_worth_snapshot")

    class BankSnapshot(
        val items: List<BankItemPayload>,
        val totalValue: Long,
        val ts: String = now(),
    ) : StatEvent("bank_snapshot")

    companion object {
        fun now(): String = Instant.now().toString()
    }
}
