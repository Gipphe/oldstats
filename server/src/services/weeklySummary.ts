import type { Database } from "better-sqlite3";
import {
  getXpBySkill,
  getLevelUps,
  getKillsByNpc,
  getTopDrops,
  getQuests,
  getSlayerTasks,
  getFarmingHistory,
  getCollectionLog,
  getCombatAchievements,
  getDiaries,
  getClues,
  getPets,
  getPersonalBests,
  getPlayerKills,
  getPlayerDeaths,
  getWorldBreakdown,
  type DateRange,
} from "./stats.js";

export interface WeeklySummary {
  weekStart: string;
  weekEnd: string;
  totalXpGained: number;
  topSkills: { skill: string; xpGained: number; level: number }[];
  levelUps: { skill: string; level: number; ts: string }[];
  totalKills: number;
  bossKills: number;
  topMonsters: { npcName: string; count: number; isBoss: boolean }[];
  totalDropValue: number;
  bestDrops: { npcName: string | null; itemName: string; quantity: number; value: number; ts: string }[];
  questsCompleted: { questName: string; ts: string }[];
  slayerTasksCompleted: number;
  slayerPointsEarned: number;
  topSlayerMonster: string | null;
  farmingHarvests: number;
  collectionLogUnlocks: { itemName: string; ts: string }[];
  combatAchievementsCompleted: { taskName: string; ts: string }[];
  combatAchievementPointsEarned: number;
  diariesCompleted: { diaryArea: string; tier: string; ts: string }[];
  cluesCompleted: { tier: string; ts: string }[];
  petsReceived: { petName: string | null; ts: string }[];
  personalBests: { activityName: string; durationSeconds: number; ts: string }[];
  playerKills: number;
  playerDeaths: number;
  netWorthChange: number | null;
  topWorld: { world: number; minutes: number } | null;
  highlights: string[];
}

function titleCase(name: string): string {
  return name
    .toLowerCase()
    .split("_")
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

function formatGp(value: number): string {
  if (value >= 1_000_000) return `${(value / 1_000_000).toFixed(2)}M gp`;
  if (value >= 1_000) return `${(value / 1_000).toFixed(1)}K gp`;
  return `${value} gp`;
}

export function getWeeklySummary(db: Database, playerId: number, weekStart: string, weekEnd: string): WeeklySummary {
  const range: DateRange = { since: weekStart, until: weekEnd };

  const xpBySkill = getXpBySkill(db, playerId, range);
  const totalXpGained = xpBySkill.reduce((sum, row) => sum + row.xpGained, 0);
  const topSkills = xpBySkill.slice(0, 5);

  const levelUps = getLevelUps(db, playerId, range);

  const killsByNpc = getKillsByNpc(db, playerId, range);
  const totalKills = killsByNpc.reduce((sum, row) => sum + row.count, 0);
  const bossKills = killsByNpc.filter((row) => row.isBoss).reduce((sum, row) => sum + row.count, 0);
  const topMonsters = killsByNpc.slice(0, 5);

  const bestDrops = getTopDrops(db, playerId, range, 5);
  const totalDropValue = db
    .prepare(
      `SELECT COALESCE(SUM(value), 0) as total FROM drops WHERE player_id = ? AND ts >= ? AND ts <= ?`
    )
    .get(playerId, weekStart, weekEnd) as { total: number };

  const questsCompleted = getQuests(db, playerId)
    .filter((q) => q.state === "COMPLETED" && q.ts >= weekStart && q.ts <= weekEnd)
    .map((q) => ({ questName: q.questName, ts: q.ts }));

  const slayerTasks = getSlayerTasks(db, playerId, { since: weekStart, until: weekEnd });
  const slayerTasksCompleted = slayerTasks.filter((t) => t.completedAt).length;
  const slayerPointsEarned = slayerTasks.reduce((sum, t) => sum + (t.points ?? 0), 0);
  const slayerCounts = new Map<string, number>();
  for (const task of slayerTasks) {
    slayerCounts.set(task.taskName, (slayerCounts.get(task.taskName) ?? 0) + (task.amountAssigned ?? 1));
  }
  const topSlayerMonster =
    [...slayerCounts.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? null;

  const farmingHistory = getFarmingHistory(db, playerId, range);
  const farmingHarvests = farmingHistory.filter((f) => f.state === "HARVESTED").length;

  const collectionLogUnlocks = getCollectionLog(db, playerId, range, 1000).map((c) => ({
    itemName: c.itemName,
    ts: c.ts,
  }));

  const combatAchievements = getCombatAchievements(db, playerId, range, 1000);
  const combatAchievementsCompleted = combatAchievements.map((c) => ({ taskName: c.taskName, ts: c.ts }));
  const pointsAsOfWeekEnd = db
    .prepare(
      `SELECT total_points FROM combat_achievements WHERE player_id = ? AND ts <= ? AND total_points IS NOT NULL ORDER BY ts DESC LIMIT 1`
    )
    .get(playerId, weekEnd) as { total_points: number } | undefined;
  const pointsBeforeWeekStart = db
    .prepare(
      `SELECT total_points FROM combat_achievements WHERE player_id = ? AND ts < ? AND total_points IS NOT NULL ORDER BY ts DESC LIMIT 1`
    )
    .get(playerId, weekStart) as { total_points: number } | undefined;
  const combatAchievementPointsEarned = Math.max(
    0,
    (pointsAsOfWeekEnd?.total_points ?? 0) - (pointsBeforeWeekStart?.total_points ?? 0)
  );

  const diariesCompleted = getDiaries(db, playerId)
    .filter((d) => d.ts >= weekStart && d.ts <= weekEnd)
    .map((d) => ({ diaryArea: d.diaryArea, tier: d.tier, ts: d.ts }));

  const cluesCompleted = getClues(db, playerId, range, 1000).map((c) => ({ tier: c.tier, ts: c.ts }));

  const petsReceived = getPets(db, playerId, range, 1000).map((p) => ({ petName: p.petName, ts: p.ts }));

  const personalBests = getPersonalBests(db, playerId)
    .filter((p) => p.ts >= weekStart && p.ts <= weekEnd)
    .map((p) => ({ activityName: p.activityName, durationSeconds: p.durationSeconds, ts: p.ts }));

  const playerKills = getPlayerKills(db, playerId, range, 10000).length;
  const playerDeaths = getPlayerDeaths(db, playerId, range, 10000).length;

  const netWorthAsOfWeekEnd = db
    .prepare(`SELECT total_value FROM net_worth_snapshots WHERE player_id = ? AND ts <= ? ORDER BY ts DESC LIMIT 1`)
    .get(playerId, weekEnd) as { total_value: number } | undefined;
  const netWorthBeforeWeekStart = db
    .prepare(`SELECT total_value FROM net_worth_snapshots WHERE player_id = ? AND ts < ? ORDER BY ts DESC LIMIT 1`)
    .get(playerId, weekStart) as { total_value: number } | undefined;
  const netWorthChange =
    netWorthAsOfWeekEnd && netWorthBeforeWeekStart
      ? netWorthAsOfWeekEnd.total_value - netWorthBeforeWeekStart.total_value
      : null;

  const worldBreakdown = getWorldBreakdown(db, playerId, range);
  const topWorld = worldBreakdown[0] ? { world: worldBreakdown[0].world, minutes: worldBreakdown[0].minutes } : null;

  const highlights: string[] = [];
  if (totalXpGained > 0) {
    highlights.push(`Gained ${totalXpGained.toLocaleString()} total XP this week.`);
  }
  if (topSkills[0]) {
    highlights.push(`Most trained skill: ${titleCase(topSkills[0].skill)} (+${topSkills[0].xpGained.toLocaleString()} xp).`);
  }
  if (levelUps.length > 0) {
    highlights.push(`Leveled up ${levelUps.length} time${levelUps.length === 1 ? "" : "s"}.`);
  }
  if (bestDrops[0]) {
    highlights.push(
      `Best drop: ${bestDrops[0].itemName} worth ${formatGp(bestDrops[0].value)}${
        bestDrops[0].npcName ? ` from ${bestDrops[0].npcName}` : ""
      }.`
    );
  }
  if (bossKills > 0) {
    highlights.push(`Killed ${bossKills} boss${bossKills === 1 ? "" : "es"}.`);
  }
  if (questsCompleted.length > 0) {
    highlights.push(`Completed ${questsCompleted.length} quest${questsCompleted.length === 1 ? "" : "s"}.`);
  }
  if (slayerTasksCompleted > 0) {
    highlights.push(
      `Finished ${slayerTasksCompleted} slayer task${slayerTasksCompleted === 1 ? "" : "s"}${
        topSlayerMonster ? `, mostly ${topSlayerMonster}` : ""
      }.`
    );
  }
  if (farmingHarvests > 0) {
    highlights.push(`Harvested ${farmingHarvests} farming patch${farmingHarvests === 1 ? "" : "es"}.`);
  }
  if (collectionLogUnlocks.length > 0) {
    highlights.push(
      `Unlocked ${collectionLogUnlocks.length} collection log item${collectionLogUnlocks.length === 1 ? "" : "s"}${
        collectionLogUnlocks.length === 1 ? `: ${collectionLogUnlocks[0].itemName}` : ""
      }.`
    );
  }
  if (combatAchievementsCompleted.length > 0) {
    highlights.push(
      `Completed ${combatAchievementsCompleted.length} combat achievement${
        combatAchievementsCompleted.length === 1 ? "" : "s"
      }${combatAchievementPointsEarned > 0 ? ` (+${combatAchievementPointsEarned} points)` : ""}.`
    );
  }
  if (diariesCompleted.length > 0) {
    highlights.push(
      `Completed ${diariesCompleted.map((d) => `${titleCase(d.diaryArea)} (${titleCase(d.tier)})`).join(", ")} diary${
        diariesCompleted.length === 1 ? "" : "ies"
      }.`
    );
  }
  if (cluesCompleted.length > 0) {
    highlights.push(`Completed ${cluesCompleted.length} clue scroll${cluesCompleted.length === 1 ? "" : "s"}.`);
  }
  if (petsReceived.length > 0) {
    highlights.push(
      `Received ${petsReceived.length} pet${petsReceived.length === 1 ? "" : "s"}${
        petsReceived.length === 1 && petsReceived[0].petName ? `: ${petsReceived[0].petName}` : ""
      }.`
    );
  }
  if (personalBests.length > 0) {
    highlights.push(`Set ${personalBests.length} new personal best${personalBests.length === 1 ? "" : "s"}.`);
  }
  if (playerKills > 0) {
    highlights.push(`Defeated ${playerKills} player${playerKills === 1 ? "" : "s"} in combat.`);
  }
  if (playerDeaths > 0) {
    highlights.push(`Died ${playerDeaths} time${playerDeaths === 1 ? "" : "s"}.`);
  }
  if (netWorthChange !== null && netWorthChange !== 0) {
    highlights.push(
      `Net worth ${netWorthChange > 0 ? "up" : "down"} ${formatGp(Math.abs(netWorthChange))} this week.`
    );
  }
  if (highlights.length === 0) {
    highlights.push("No tracked activity this week — time to log back in!");
  }

  return {
    weekStart,
    weekEnd,
    totalXpGained,
    topSkills,
    levelUps,
    totalKills,
    bossKills,
    topMonsters,
    totalDropValue: totalDropValue.total,
    bestDrops,
    questsCompleted,
    slayerTasksCompleted,
    slayerPointsEarned,
    topSlayerMonster,
    farmingHarvests,
    collectionLogUnlocks,
    combatAchievementsCompleted,
    combatAchievementPointsEarned,
    diariesCompleted,
    cluesCompleted,
    petsReceived,
    personalBests,
    playerKills,
    playerDeaths,
    netWorthChange,
    topWorld,
    highlights,
  };
}
