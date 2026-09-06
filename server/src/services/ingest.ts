import type { Database } from "better-sqlite3";
import type { IngestEvent } from "../types/events.js";

export function ingestEvents(db: Database, playerId: number, events: IngestEvent[]): void {
  const insertXpGain = db.prepare(
    `INSERT INTO xp_gains (player_id, skill, xp, xp_gained, level, ts) VALUES (?, ?, ?, ?, ?, ?)`
  );
  const insertLevelUp = db.prepare(
    `INSERT INTO level_ups (player_id, skill, level, ts) VALUES (?, ?, ?, ?)`
  );
  const insertKill = db.prepare(
    `INSERT INTO kills (player_id, npc_name, npc_id, is_boss, ts) VALUES (?, ?, ?, ?, ?)`
  );
  const insertDrop = db.prepare(
    `INSERT INTO drops (player_id, npc_name, item_name, item_id, quantity, value, ts) VALUES (?, ?, ?, ?, ?, ?, ?)`
  );
  const upsertQuest = db.prepare(
    `INSERT INTO quests (player_id, quest_name, state, quest_points, ts)
     VALUES (?, ?, ?, ?, ?)
     ON CONFLICT(player_id, quest_name, state) DO UPDATE SET
       quest_points = excluded.quest_points,
       ts = excluded.ts`
  );
  const insertSlayerTask = db.prepare(
    `INSERT INTO slayer_tasks (player_id, task_name, amount_assigned, points, streak, started_at, completed_at)
     VALUES (?, ?, ?, ?, ?, ?, ?)`
  );
  const insertFarmingPatch = db.prepare(
    `INSERT INTO farming_patches (player_id, patch_name, crop, state, ts) VALUES (?, ?, ?, ?, ?)`
  );
  const upsertCollectionLogItem = db.prepare(
    `INSERT INTO collection_log_items (player_id, item_name, item_id, total_unlocked, total_possible, ts)
     VALUES (?, ?, ?, ?, ?, ?)
     ON CONFLICT(player_id, item_name) DO UPDATE SET
       total_unlocked = excluded.total_unlocked,
       total_possible = excluded.total_possible,
       ts = excluded.ts`
  );
  const upsertCombatAchievement = db.prepare(
    `INSERT INTO combat_achievements (player_id, task_name, total_points, ts)
     VALUES (?, ?, ?, ?)
     ON CONFLICT(player_id, task_name) DO UPDATE SET
       total_points = excluded.total_points,
       ts = excluded.ts`
  );
  const upsertDiary = db.prepare(
    `INSERT INTO achievement_diaries (player_id, diary_area, tier, ts)
     VALUES (?, ?, ?, ?)
     ON CONFLICT(player_id, diary_area, tier) DO UPDATE SET
       ts = excluded.ts`
  );
  const upsertDiaryTaskProgress = db.prepare(
    `INSERT INTO diary_task_progress (player_id, diary_area, tier, task_name, completed, ts)
     VALUES (?, ?, ?, ?, ?, ?)
     ON CONFLICT(player_id, diary_area, tier, task_name) DO UPDATE SET
       completed = excluded.completed,
       ts = excluded.ts`
  );
  const insertClue = db.prepare(
    `INSERT INTO clue_completions (player_id, tier, count, ts) VALUES (?, ?, ?, ?)`
  );
  const insertPet = db.prepare(
    `INSERT INTO pet_drops (player_id, pet_name, ts) VALUES (?, ?, ?)`
  );
  const upsertPersonalBest = db.prepare(
    `INSERT INTO personal_bests (player_id, activity_name, duration_seconds, ts)
     VALUES (?, ?, ?, ?)
     ON CONFLICT(player_id, activity_name) DO UPDATE SET
       duration_seconds = excluded.duration_seconds,
       ts = excluded.ts
     WHERE excluded.duration_seconds < personal_bests.duration_seconds`
  );
  const insertPlayerKill = db.prepare(
    `INSERT INTO player_kills (player_id, opponent_name, ts) VALUES (?, ?, ?)`
  );
  const insertPlayerDeath = db.prepare(
    `INSERT INTO player_deaths (player_id, value_lost, ts) VALUES (?, ?, ?)`
  );
  const insertWorldChange = db.prepare(
    `INSERT INTO world_changes (player_id, world, world_types, ts) VALUES (?, ?, ?, ?)`
  );
  const insertNetWorthSnapshot = db.prepare(
    `INSERT INTO net_worth_snapshots (player_id, inventory_value, equipment_value, bank_value, total_value, ts)
     VALUES (?, ?, ?, ?, ?, ?)`
  );
  const deleteBankItems = db.prepare(`DELETE FROM bank_items WHERE player_id = ?`);
  const insertBankItem = db.prepare(
    `INSERT INTO bank_items (player_id, item_id, item_name, quantity, value) VALUES (?, ?, ?, ?, ?)`
  );
  const upsertBankMeta = db.prepare(
    `INSERT INTO bank_snapshots_meta (player_id, total_value, item_count, ts)
     VALUES (?, ?, ?, ?)
     ON CONFLICT(player_id) DO UPDATE SET
       total_value = excluded.total_value,
       item_count = excluded.item_count,
       ts = excluded.ts`
  );

  const applyAll = db.transaction((evts: IngestEvent[]) => {
    for (const event of evts) {
      switch (event.type) {
        case "xp_gain":
          insertXpGain.run(playerId, event.skill, event.xp, event.xpGained, event.level, event.ts);
          break;
        case "level_up":
          insertLevelUp.run(playerId, event.skill, event.level, event.ts);
          break;
        case "kill":
          insertKill.run(playerId, event.npcName, event.npcId ?? null, event.isBoss ? 1 : 0, event.ts);
          break;
        case "drop":
          insertDrop.run(
            playerId,
            event.npcName ?? null,
            event.itemName,
            event.itemId ?? null,
            event.quantity,
            event.value,
            event.ts
          );
          break;
        case "quest":
          upsertQuest.run(playerId, event.questName, event.state, event.questPoints ?? null, event.ts);
          break;
        case "slayer_task":
          insertSlayerTask.run(
            playerId,
            event.taskName,
            event.amountAssigned ?? null,
            event.points ?? null,
            event.streak ?? null,
            event.startedAt ?? null,
            event.completedAt ?? null
          );
          break;
        case "farming_patch":
          insertFarmingPatch.run(playerId, event.patchName, event.crop ?? null, event.state, event.ts);
          break;
        case "collection_log_item":
          upsertCollectionLogItem.run(
            playerId,
            event.itemName,
            event.itemId ?? null,
            event.totalUnlocked ?? null,
            event.totalPossible ?? null,
            event.ts
          );
          break;
        case "combat_achievement":
          upsertCombatAchievement.run(playerId, event.taskName, event.totalPoints ?? null, event.ts);
          break;
        case "diary_completed":
          upsertDiary.run(playerId, event.diaryArea, event.tier, event.ts);
          break;
        case "diary_task_progress":
          upsertDiaryTaskProgress.run(
            playerId,
            event.diaryArea,
            event.tier,
            event.taskName,
            event.completed ? 1 : 0,
            event.ts
          );
          break;
        case "clue_completed":
          insertClue.run(playerId, event.tier, event.count ?? null, event.ts);
          break;
        case "pet_received":
          insertPet.run(playerId, event.petName ?? null, event.ts);
          break;
        case "personal_best":
          upsertPersonalBest.run(playerId, event.activityName, event.durationSeconds, event.ts);
          break;
        case "player_kill":
          insertPlayerKill.run(playerId, event.opponentName, event.ts);
          break;
        case "player_death":
          insertPlayerDeath.run(playerId, event.valueLost ?? null, event.ts);
          break;
        case "world_change":
          insertWorldChange.run(playerId, event.world, JSON.stringify(event.worldTypes), event.ts);
          break;
        case "net_worth_snapshot":
          insertNetWorthSnapshot.run(
            playerId,
            event.inventoryValue,
            event.equipmentValue,
            event.bankValue,
            event.totalValue,
            event.ts
          );
          break;
        case "bank_snapshot":
          deleteBankItems.run(playerId);
          for (const item of event.items) {
            insertBankItem.run(playerId, item.itemId ?? null, item.itemName, item.quantity, item.value);
          }
          upsertBankMeta.run(playerId, event.totalValue, event.items.length, event.ts);
          break;
      }
    }
  });

  applyAll(events);
}
