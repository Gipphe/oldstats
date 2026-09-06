export interface Player {
  id: number;
  username: string;
  createdAt: string;
}

export interface Overview {
  totalXpGained: number;
  totalKills: number;
  totalBossKills: number;
  totalDropValue: number;
  questsCompleted: number;
  slayerTasksCompleted: number;
  questPoints: number;
  collectionLogItems: number;
  collectionLogTotalUnlocked: number;
  collectionLogTotalPossible: number | null;
  combatAchievementsCompleted: number;
  combatAchievementPoints: number;
  diariesCompleted: number;
  cluesCompleted: number;
  petsReceived: number;
  playerKills: number;
  playerDeaths: number;
  netWorth: number | null;
}

export interface XpBySkill {
  skill: string;
  xpGained: number;
  level: number;
}

export interface LevelUp {
  skill: string;
  level: number;
  ts: string;
}

export interface Kill {
  npcName: string;
  npcId: number | null;
  isBoss: number;
  ts: string;
}

export interface KillByNpc {
  npcName: string;
  isBoss: number;
  count: number;
}

export interface Drop {
  npcName: string | null;
  itemName: string;
  itemId: number | null;
  quantity: number;
  value: number;
  ts: string;
}

export interface Quest {
  questName: string;
  state: "IN_PROGRESS" | "COMPLETED";
  questPoints: number | null;
  ts: string;
}

export interface SlayerTask {
  taskName: string;
  amountAssigned: number | null;
  points: number | null;
  streak: number | null;
  startedAt: string | null;
  completedAt: string | null;
}

export interface FarmingPatch {
  patchName: string;
  crop: string | null;
  state: string;
  ts: string;
}

export interface CollectionLogItem {
  itemName: string;
  itemId: number | null;
  totalUnlocked: number | null;
  totalPossible: number | null;
  ts: string;
}

export interface CombatAchievement {
  taskName: string;
  totalPoints: number | null;
  ts: string;
}

export interface Diary {
  diaryArea: string;
  tier: string;
  ts: string;
}

export interface Clue {
  tier: string;
  count: number | null;
  ts: string;
}

export interface Pet {
  petName: string | null;
  ts: string;
}

export interface PersonalBest {
  activityName: string;
  durationSeconds: number;
  ts: string;
}

export interface PlayerKill {
  opponentName: string;
  ts: string;
}

export interface PlayerDeath {
  valueLost: number | null;
  ts: string;
}

export interface WorldBreakdown {
  world: number;
  worldTypes: string[];
  minutes: number;
  visits: number;
}

export interface NetWorthSnapshot {
  inventoryValue: number;
  equipmentValue: number;
  bankValue: number;
  totalValue: number;
  ts: string;
}

export interface WeeklySummary {
  weekStart: string;
  weekEnd: string;
  totalXpGained: number;
  topSkills: XpBySkill[];
  levelUps: LevelUp[];
  totalKills: number;
  bossKills: number;
  topMonsters: KillByNpc[];
  totalDropValue: number;
  bestDrops: Drop[];
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
