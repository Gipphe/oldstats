import { z } from "zod";

const isoTimestamp = z.string().datetime({ offset: true }).or(z.string().min(1));

export const xpGainEvent = z.object({
  type: z.literal("xp_gain"),
  skill: z.string(),
  xp: z.number().int().nonnegative(),
  xpGained: z.number().int().nonnegative(),
  level: z.number().int().positive(),
  ts: isoTimestamp,
});

export const levelUpEvent = z.object({
  type: z.literal("level_up"),
  skill: z.string(),
  level: z.number().int().positive(),
  ts: isoTimestamp,
});

export const killEvent = z.object({
  type: z.literal("kill"),
  npcName: z.string(),
  npcId: z.number().int().optional(),
  isBoss: z.boolean().default(false),
  ts: isoTimestamp,
});

export const dropEvent = z.object({
  type: z.literal("drop"),
  npcName: z.string().optional(),
  itemName: z.string(),
  itemId: z.number().int().optional(),
  quantity: z.number().int().positive().default(1),
  value: z.number().int().nonnegative().default(0),
  ts: isoTimestamp,
});

export const questEvent = z.object({
  type: z.literal("quest"),
  questName: z.string(),
  state: z.enum(["IN_PROGRESS", "COMPLETED"]),
  questPoints: z.number().int().nonnegative().optional(),
  ts: isoTimestamp,
});

export const slayerTaskEvent = z.object({
  type: z.literal("slayer_task"),
  taskName: z.string(),
  amountAssigned: z.number().int().nonnegative().optional(),
  points: z.number().int().nonnegative().optional(),
  streak: z.number().int().nonnegative().optional(),
  startedAt: isoTimestamp.optional(),
  completedAt: isoTimestamp.optional(),
});

export const farmingPatchEvent = z.object({
  type: z.literal("farming_patch"),
  patchName: z.string(),
  crop: z.string().optional(),
  state: z.enum(["EMPTY", "PLANTED", "GROWING", "DISEASED", "DEAD", "HARVESTABLE", "HARVESTED"]),
  ts: isoTimestamp,
});

export const collectionLogItemEvent = z.object({
  type: z.literal("collection_log_item"),
  itemName: z.string(),
  itemId: z.number().int().optional(),
  totalUnlocked: z.number().int().nonnegative().optional(),
  totalPossible: z.number().int().nonnegative().optional(),
  ts: isoTimestamp,
});

export const combatAchievementEvent = z.object({
  type: z.literal("combat_achievement"),
  taskName: z.string(),
  totalPoints: z.number().int().nonnegative().optional(),
  ts: isoTimestamp,
});

export const diaryCompletedEvent = z.object({
  type: z.literal("diary_completed"),
  diaryArea: z.string(),
  tier: z.enum(["EASY", "MEDIUM", "HARD", "ELITE"]),
  ts: isoTimestamp,
});

export const diaryTaskProgressEvent = z.object({
  type: z.literal("diary_task_progress"),
  diaryArea: z.string(),
  tier: z.enum(["EASY", "MEDIUM", "HARD", "ELITE"]),
  taskName: z.string(),
  completed: z.boolean(),
  ts: isoTimestamp,
});

export const clueCompletedEvent = z.object({
  type: z.literal("clue_completed"),
  tier: z.string(),
  count: z.number().int().nonnegative().optional(),
  ts: isoTimestamp,
});

export const petReceivedEvent = z.object({
  type: z.literal("pet_received"),
  petName: z.string().optional(),
  ts: isoTimestamp,
});

export const personalBestEvent = z.object({
  type: z.literal("personal_best"),
  activityName: z.string(),
  durationSeconds: z.number().nonnegative(),
  ts: isoTimestamp,
});

export const playerKillEvent = z.object({
  type: z.literal("player_kill"),
  opponentName: z.string(),
  ts: isoTimestamp,
});

export const playerDeathEvent = z.object({
  type: z.literal("player_death"),
  valueLost: z.number().int().nonnegative().optional(),
  ts: isoTimestamp,
});

export const worldChangeEvent = z.object({
  type: z.literal("world_change"),
  world: z.number().int().positive(),
  worldTypes: z.array(z.string()).default([]),
  ts: isoTimestamp,
});

export const netWorthSnapshotEvent = z.object({
  type: z.literal("net_worth_snapshot"),
  inventoryValue: z.number().int().nonnegative(),
  equipmentValue: z.number().int().nonnegative(),
  bankValue: z.number().int().nonnegative(),
  totalValue: z.number().int().nonnegative(),
  ts: isoTimestamp,
});

export const bankItemSchema = z.object({
  itemId: z.number().int().optional(),
  itemName: z.string(),
  quantity: z.number().int().nonnegative(),
  value: z.number().int().nonnegative(),
});

export const bankSnapshotEvent = z.object({
  type: z.literal("bank_snapshot"),
  items: z.array(bankItemSchema).max(2000),
  totalValue: z.number().int().nonnegative(),
  ts: isoTimestamp,
});

export const ingestEvent = z.discriminatedUnion("type", [
  xpGainEvent,
  levelUpEvent,
  killEvent,
  dropEvent,
  questEvent,
  slayerTaskEvent,
  farmingPatchEvent,
  collectionLogItemEvent,
  combatAchievementEvent,
  diaryCompletedEvent,
  diaryTaskProgressEvent,
  clueCompletedEvent,
  petReceivedEvent,
  personalBestEvent,
  playerKillEvent,
  playerDeathEvent,
  worldChangeEvent,
  netWorthSnapshotEvent,
  bankSnapshotEvent,
]);

export type IngestEvent = z.infer<typeof ingestEvent>;

export const ingestBatch = z.object({
  events: z.array(ingestEvent).min(1).max(1000),
});

export type IngestBatch = z.infer<typeof ingestBatch>;
