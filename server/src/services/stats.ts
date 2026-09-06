import type { Database } from "better-sqlite3";

export interface DateRange {
  since?: string;
  until?: string;
}

function rangeClause(range: DateRange, column = "ts"): { clause: string; params: string[] } {
  const parts: string[] = [];
  const params: string[] = [];
  if (range.since) {
    parts.push(`${column} >= ?`);
    params.push(range.since);
  }
  if (range.until) {
    parts.push(`${column} <= ?`);
    params.push(range.until);
  }
  return { clause: parts.length ? `AND ${parts.join(" AND ")}` : "", params };
}

export function getOverview(db: Database, playerId: number) {
  const totalXp = db
    .prepare(`SELECT COALESCE(SUM(xp_gained), 0) as total FROM xp_gains WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const totalKills = db
    .prepare(`SELECT COUNT(*) as total FROM kills WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const totalBossKills = db
    .prepare(`SELECT COUNT(*) as total FROM kills WHERE player_id = ? AND is_boss = 1`)
    .get(playerId) as { total: number };
  const totalDropValue = db
    .prepare(`SELECT COALESCE(SUM(value), 0) as total FROM drops WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const questsCompleted = db
    .prepare(`SELECT COUNT(*) as total FROM quests WHERE player_id = ? AND state = 'COMPLETED'`)
    .get(playerId) as { total: number };
  const slayerTasksCompleted = db
    .prepare(`SELECT COUNT(*) as total FROM slayer_tasks WHERE player_id = ? AND completed_at IS NOT NULL`)
    .get(playerId) as { total: number };
  const latestQuestPoints = db
    .prepare(
      `SELECT quest_points FROM quests WHERE player_id = ? AND quest_points IS NOT NULL ORDER BY ts DESC LIMIT 1`
    )
    .get(playerId) as { quest_points: number } | undefined;
  const collectionLogItems = db
    .prepare(`SELECT COUNT(*) as total FROM collection_log_items WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const latestCollectionLog = db
    .prepare(
      `SELECT total_unlocked, total_possible FROM collection_log_items
       WHERE player_id = ? AND total_unlocked IS NOT NULL ORDER BY ts DESC LIMIT 1`
    )
    .get(playerId) as { total_unlocked: number; total_possible: number } | undefined;
  const combatAchievementsCompleted = db
    .prepare(`SELECT COUNT(*) as total FROM combat_achievements WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const latestCombatAchievementPoints = db
    .prepare(
      `SELECT total_points FROM combat_achievements WHERE player_id = ? AND total_points IS NOT NULL ORDER BY ts DESC LIMIT 1`
    )
    .get(playerId) as { total_points: number } | undefined;
  const diariesCompleted = db
    .prepare(`SELECT COUNT(*) as total FROM achievement_diaries WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const cluesCompleted = db
    .prepare(`SELECT COUNT(*) as total FROM clue_completions WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const petsReceived = db
    .prepare(`SELECT COUNT(*) as total FROM pet_drops WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const playerKillCount = db
    .prepare(`SELECT COUNT(*) as total FROM player_kills WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const playerDeathCount = db
    .prepare(`SELECT COUNT(*) as total FROM player_deaths WHERE player_id = ?`)
    .get(playerId) as { total: number };
  const latestNetWorth = db
    .prepare(`SELECT total_value FROM net_worth_snapshots WHERE player_id = ? ORDER BY ts DESC LIMIT 1`)
    .get(playerId) as { total_value: number } | undefined;

  return {
    totalXpGained: totalXp.total,
    totalKills: totalKills.total,
    totalBossKills: totalBossKills.total,
    totalDropValue: totalDropValue.total,
    questsCompleted: questsCompleted.total,
    slayerTasksCompleted: slayerTasksCompleted.total,
    questPoints: latestQuestPoints?.quest_points ?? 0,
    collectionLogItems: collectionLogItems.total,
    collectionLogTotalUnlocked: latestCollectionLog?.total_unlocked ?? collectionLogItems.total,
    collectionLogTotalPossible: latestCollectionLog?.total_possible ?? null,
    combatAchievementsCompleted: combatAchievementsCompleted.total,
    combatAchievementPoints: latestCombatAchievementPoints?.total_points ?? 0,
    diariesCompleted: diariesCompleted.total,
    cluesCompleted: cluesCompleted.total,
    petsReceived: petsReceived.total,
    playerKills: playerKillCount.total,
    playerDeaths: playerDeathCount.total,
    netWorth: latestNetWorth?.total_value ?? null,
  };
}

export function getXpSeries(db: Database, playerId: number, range: DateRange, skill?: string) {
  const { clause, params } = rangeClause(range);
  const skillClause = skill ? "AND skill = ?" : "";
  const rows = db
    .prepare(
      `SELECT skill, xp, xp_gained as xpGained, level, ts FROM xp_gains
       WHERE player_id = ? ${clause} ${skillClause}
       ORDER BY ts ASC`
    )
    .all(playerId, ...params, ...(skill ? [skill] : [])) as any[];
  return rows;
}

export function getXpBySkill(db: Database, playerId: number, range: DateRange) {
  const { clause, params } = rangeClause(range);
  const rows = db
    .prepare(
      `SELECT skill, COALESCE(SUM(xp_gained), 0) as xpGained, MAX(level) as level
       FROM xp_gains WHERE player_id = ? ${clause}
       GROUP BY skill ORDER BY xpGained DESC`
    )
    .all(playerId, ...params) as any[];
  return rows;
}

export function getLevelUps(db: Database, playerId: number, range: DateRange) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT skill, level, ts FROM level_ups WHERE player_id = ? ${clause} ORDER BY ts DESC`
    )
    .all(playerId, ...params) as any[];
}

export function getKills(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT npc_name as npcName, npc_id as npcId, is_boss as isBoss, ts
       FROM kills WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getKillsByNpc(db: Database, playerId: number, range: DateRange) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT npc_name as npcName, is_boss as isBoss, COUNT(*) as count
       FROM kills WHERE player_id = ? ${clause}
       GROUP BY npc_name ORDER BY count DESC`
    )
    .all(playerId, ...params) as any[];
}

export function getDrops(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT npc_name as npcName, item_name as itemName, item_id as itemId, quantity, value, ts
       FROM drops WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getTopDrops(db: Database, playerId: number, range: DateRange, limit = 10) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT npc_name as npcName, item_name as itemName, item_id as itemId, quantity, value, ts
       FROM drops WHERE player_id = ? ${clause} ORDER BY value DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getQuests(db: Database, playerId: number) {
  return db
    .prepare(
      `SELECT quest_name as questName, state, quest_points as questPoints, ts
       FROM quests WHERE player_id = ? ORDER BY ts DESC`
    )
    .all(playerId) as any[];
}

export function getSlayerTasks(db: Database, playerId: number, range: DateRange, limit = 50) {
  const { clause, params } = rangeClause(range, "completed_at");
  return db
    .prepare(
      `SELECT task_name as taskName, amount_assigned as amountAssigned, points, streak, started_at as startedAt, completed_at as completedAt
       FROM slayer_tasks WHERE player_id = ? ${clause}
       ORDER BY COALESCE(completed_at, started_at) DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getFarmingPatches(db: Database, playerId: number) {
  return db
    .prepare(
      `SELECT patch_name as patchName, crop, state, ts
       FROM farming_patches fp
       WHERE player_id = ? AND ts = (
         SELECT MAX(ts) FROM farming_patches WHERE patch_name = fp.patch_name AND player_id = fp.player_id
       )
       ORDER BY patch_name`
    )
    .all(playerId) as any[];
}

export function getFarmingHistory(db: Database, playerId: number, range: DateRange) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT patch_name as patchName, crop, state, ts
       FROM farming_patches WHERE player_id = ? ${clause} ORDER BY ts DESC`
    )
    .all(playerId, ...params) as any[];
}

export function getCollectionLog(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT item_name as itemName, item_id as itemId, total_unlocked as totalUnlocked, total_possible as totalPossible, ts
       FROM collection_log_items WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getCombatAchievements(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT task_name as taskName, total_points as totalPoints, ts
       FROM combat_achievements WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getDiaries(db: Database, playerId: number) {
  return db
    .prepare(
      `SELECT diary_area as diaryArea, tier, ts
       FROM achievement_diaries WHERE player_id = ? ORDER BY ts DESC`
    )
    .all(playerId) as any[];
}

export function getDiaryTaskProgress(db: Database, playerId: number) {
  return db
    .prepare(
      `SELECT diary_area as diaryArea, tier, task_name as taskName, completed, ts
       FROM diary_task_progress WHERE player_id = ?
       ORDER BY diary_area, CASE tier WHEN 'EASY' THEN 0 WHEN 'MEDIUM' THEN 1 WHEN 'HARD' THEN 2 WHEN 'ELITE' THEN 3 ELSE 4 END, id`
    )
    .all(playerId) as any[];
}

export function getClues(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT tier, count, ts FROM clue_completions WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getPets(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT pet_name as petName, ts FROM pet_drops WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getPersonalBests(db: Database, playerId: number) {
  return db
    .prepare(
      `SELECT activity_name as activityName, duration_seconds as durationSeconds, ts
       FROM personal_bests WHERE player_id = ? ORDER BY ts DESC`
    )
    .all(playerId) as any[];
}

export function getPlayerKills(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT opponent_name as opponentName, ts FROM player_kills WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getPlayerDeaths(db: Database, playerId: number, range: DateRange, limit = 100) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT value_lost as valueLost, ts FROM player_deaths WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

/**
 * Aggregates world_changes into per-world time-spent using each event's gap
 * to the next as that world's duration. Any single gap (including the most
 * recent world with no follow-up event yet) is capped at 6 hours so a
 * forgotten/offline session doesn't inflate one world's total indefinitely.
 */
export function getWorldBreakdown(db: Database, playerId: number, range: DateRange) {
  const { clause, params } = rangeClause(range);
  const rows = db
    .prepare(
      `WITH ordered AS (
         SELECT world, world_types, ts,
                COALESCE(LEAD(ts) OVER (ORDER BY ts), datetime('now')) as end_ts
         FROM world_changes
         WHERE player_id = ? ${clause}
       )
       SELECT world, MAX(world_types) as worldTypes,
              SUM(MIN((julianday(end_ts) - julianday(ts)) * 1440, 360)) as minutes,
              COUNT(*) as visits
       FROM ordered
       GROUP BY world
       ORDER BY minutes DESC`
    )
    .all(playerId, ...params) as { world: number; worldTypes: string; minutes: number; visits: number }[];

  return rows.map((row) => ({
    world: row.world,
    worldTypes: JSON.parse(row.worldTypes) as string[],
    minutes: Math.round(row.minutes),
    visits: row.visits,
  }));
}

export function getNetWorthHistory(db: Database, playerId: number, range: DateRange, limit = 500) {
  const { clause, params } = rangeClause(range);
  return db
    .prepare(
      `SELECT inventory_value as inventoryValue, equipment_value as equipmentValue,
              bank_value as bankValue, total_value as totalValue, ts
       FROM net_worth_snapshots WHERE player_id = ? ${clause} ORDER BY ts DESC LIMIT ?`
    )
    .all(playerId, ...params, limit) as any[];
}

export function getBank(db: Database, playerId: number) {
  const items = db
    .prepare(
      `SELECT item_id as itemId, item_name as itemName, quantity, value
       FROM bank_items WHERE player_id = ? ORDER BY value DESC`
    )
    .all(playerId) as any[];
  const meta = db
    .prepare(`SELECT total_value as totalValue, item_count as itemCount, ts FROM bank_snapshots_meta WHERE player_id = ?`)
    .get(playerId) as { totalValue: number; itemCount: number; ts: string } | undefined;

  return {
    items,
    totalValue: meta?.totalValue ?? 0,
    itemCount: meta?.itemCount ?? 0,
    lastSyncedAt: meta?.ts ?? null,
  };
}
