/**
 * Single source of truth for the e2e test fixture: seeded once in
 * global-setup, then referenced by spec files for assertions, so the two
 * never drift apart.
 */
export const PLAYER_USERNAME = "e2e_zezima";

const DAY_MS = 24 * 60 * 60 * 1000;

export function buildSeedEvents(now: Date = new Date()) {
  const nowIso = now.toISOString();
  const yesterdayIso = new Date(now.getTime() - DAY_MS).toISOString();

  return [
    { type: "xp_gain", skill: "SLAYER", xp: 120000, xpGained: 5000, level: 80, ts: nowIso },
    { type: "xp_gain", skill: "MAGIC", xp: 50000, xpGained: 8000, level: 70, ts: nowIso },
    { type: "level_up", skill: "SLAYER", level: 80, ts: nowIso },

    { type: "kill", npcName: "Vorkath", npcId: 8058, isBoss: true, ts: nowIso },
    { type: "kill", npcName: "Abyssal demon", npcId: 415, isBoss: false, ts: nowIso },
    { type: "kill", npcName: "Abyssal demon", npcId: 415, isBoss: false, ts: yesterdayIso },

    { type: "drop", npcName: "Vorkath", itemName: "Draconic visage", itemId: 21295, quantity: 1, value: 8500000, ts: nowIso },

    { type: "quest", questName: "DRAGON_SLAYER_II", state: "COMPLETED", questPoints: 300, ts: nowIso },

    {
      type: "slayer_task",
      taskName: "Abyssal demons",
      amountAssigned: 150,
      points: 6,
      startedAt: yesterdayIso,
      completedAt: nowIso,
    },

    { type: "farming_patch", patchName: "Catherby (South)", crop: "Ranarr", state: "HARVESTED", ts: nowIso },

    {
      type: "collection_log_item",
      itemName: "Draconic visage",
      itemId: 21295,
      totalUnlocked: 46,
      totalPossible: 1600,
      ts: nowIso,
    },

    { type: "combat_achievement", taskName: "Vorkath Killcount 1", totalPoints: 155, ts: nowIso },

    { type: "diary_completed", diaryArea: "ARDOUGNE", tier: "ELITE", ts: nowIso },

    { type: "diary_task_progress", diaryArea: "ARDOUGNE", tier: "EASY", taskName: "Enter the Wilderness", completed: true, ts: nowIso },
    {
      type: "diary_task_progress",
      diaryArea: "ARDOUGNE",
      tier: "EASY",
      taskName: "Steal from the Ardougne market stalls",
      completed: false,
      ts: nowIso,
    },

    { type: "clue_completed", tier: "Elite", count: 12, ts: nowIso },

    { type: "pet_received", petName: "Vorki", ts: nowIso },

    { type: "personal_best", activityName: "Vorkath", durationSeconds: 75.6, ts: nowIso },

    { type: "player_kill", opponentName: "Some Pker", ts: nowIso },
    { type: "player_death", valueLost: 500000, ts: yesterdayIso },

    { type: "world_change", world: 420, worldTypes: ["MEMBERS"], ts: yesterdayIso },
    { type: "world_change", world: 421, worldTypes: ["MEMBERS", "PVP"], ts: nowIso },

    {
      type: "net_worth_snapshot",
      inventoryValue: 100000,
      equipmentValue: 2000000,
      bankValue: 5000000,
      totalValue: 7100000,
      ts: yesterdayIso,
    },
    {
      type: "net_worth_snapshot",
      inventoryValue: 150000,
      equipmentValue: 2000000,
      bankValue: 8000000,
      totalValue: 10150000,
      ts: nowIso,
    },

    {
      type: "bank_snapshot",
      totalValue: 15230000,
      ts: nowIso,
      items: [
        { itemId: 21295, itemName: "Draconic visage", quantity: 1, value: 8500000 },
        { itemId: 12934, itemName: "Zulrah's scales", quantity: 15000, value: 3000000 },
        { itemId: 995, itemName: "Coins", quantity: 2500000, value: 2500000 },
        { itemId: 561, itemName: "Nature rune", quantity: 5000, value: 1000000 },
        { itemId: 1513, itemName: "Magic logs", quantity: 300, value: 230000 },
      ],
    },
  ];
}
