import { beforeEach, describe, expect, it } from "vitest";
import type { Database } from "better-sqlite3";
import { freshDb } from "../test-helpers.js";
import { createPlayer } from "./players.js";
import { ingestEvents } from "./ingest.js";
import type { IngestEvent } from "../types/events.js";
import { getBank, getDiaryTaskProgress, getOverview, getWorldBreakdown, getXpBySkill, getXpSeries } from "./stats.js";

describe("stats service", () => {
  let db: Database;
  let playerId: number;

  beforeEach(() => {
    db = freshDb();
    playerId = createPlayer(db, "zezima").id;
  });

  function ingest(events: IngestEvent[]) {
    ingestEvents(db, playerId, events);
  }

  describe("getOverview", () => {
    it("aggregates zeros for a player with no activity", () => {
      const overview = getOverview(db, playerId);
      expect(overview.totalXpGained).toBe(0);
      expect(overview.totalKills).toBe(0);
      expect(overview.netWorth).toBeNull();
      expect(overview.collectionLogTotalPossible).toBeNull();
    });

    it("sums xp, counts kills/bosses and totals drop value", () => {
      ingest([
        { type: "xp_gain", skill: "SLAYER", xp: 100, xpGained: 100, level: 10, ts: "2026-01-01T00:00:00.000Z" },
        { type: "xp_gain", skill: "SLAYER", xp: 150, xpGained: 50, level: 10, ts: "2026-01-01T00:01:00.000Z" },
        { type: "kill", npcName: "Vorkath", isBoss: true, ts: "2026-01-01T00:00:00.000Z" },
        { type: "kill", npcName: "Chicken", isBoss: false, ts: "2026-01-01T00:00:00.000Z" },
        { type: "drop", itemName: "Draconic visage", quantity: 1, value: 8500000, ts: "2026-01-01T00:00:00.000Z" },
      ]);
      const overview = getOverview(db, playerId);
      expect(overview.totalXpGained).toBe(150);
      expect(overview.totalKills).toBe(2);
      expect(overview.totalBossKills).toBe(1);
      expect(overview.totalDropValue).toBe(8500000);
    });

    it("reports the latest net worth and collection log totals", () => {
      ingest([
        {
          type: "net_worth_snapshot",
          inventoryValue: 1,
          equipmentValue: 1,
          bankValue: 1,
          totalValue: 100,
          ts: "2026-01-01T00:00:00.000Z",
        },
        {
          type: "net_worth_snapshot",
          inventoryValue: 1,
          equipmentValue: 1,
          bankValue: 1,
          totalValue: 200,
          ts: "2026-01-02T00:00:00.000Z",
        },
        {
          type: "collection_log_item",
          itemName: "X",
          totalUnlocked: 10,
          totalPossible: 1600,
          ts: "2026-01-01T00:00:00.000Z",
        },
      ]);
      const overview = getOverview(db, playerId);
      expect(overview.netWorth).toBe(200);
      expect(overview.collectionLogTotalUnlocked).toBe(10);
      expect(overview.collectionLogTotalPossible).toBe(1600);
    });
  });

  describe("getXpBySkill / getXpSeries", () => {
    it("groups and sums xp gained per skill", () => {
      ingest([
        { type: "xp_gain", skill: "SLAYER", xp: 100, xpGained: 100, level: 10, ts: "2026-01-01T00:00:00.000Z" },
        { type: "xp_gain", skill: "SLAYER", xp: 150, xpGained: 50, level: 11, ts: "2026-01-01T00:01:00.000Z" },
        { type: "xp_gain", skill: "MAGIC", xp: 300, xpGained: 300, level: 20, ts: "2026-01-01T00:00:00.000Z" },
      ]);
      const bySkill = getXpBySkill(db, playerId, {});
      expect(bySkill).toEqual([
        { skill: "MAGIC", xpGained: 300, level: 20 },
        { skill: "SLAYER", xpGained: 150, level: 11 },
      ]);
    });

    it("filters xp series by date range", () => {
      ingest([
        { type: "xp_gain", skill: "SLAYER", xp: 100, xpGained: 100, level: 10, ts: "2026-01-01T00:00:00.000Z" },
        { type: "xp_gain", skill: "SLAYER", xp: 200, xpGained: 100, level: 11, ts: "2026-01-10T00:00:00.000Z" },
      ]);
      const inRange = getXpSeries(db, playerId, { since: "2026-01-05T00:00:00.000Z" });
      expect(inRange).toHaveLength(1);
      expect(inRange[0].ts).toBe("2026-01-10T00:00:00.000Z");
    });
  });

  describe("getWorldBreakdown", () => {
    it("attributes time to each world using the gap to the next event", () => {
      ingest([
        { type: "world_change", world: 420, worldTypes: ["MEMBERS"], ts: "2026-01-01T00:00:00.000Z" },
        { type: "world_change", world: 421, worldTypes: ["MEMBERS", "PVP"], ts: "2026-01-01T00:30:00.000Z" },
      ]);
      const breakdown = getWorldBreakdown(db, playerId, {});
      const w420 = breakdown.find((w) => w.world === 420);
      expect(w420?.minutes).toBe(30);
      expect(w420?.visits).toBe(1);
    });

    it("caps a single gap at 6 hours so a forgotten session doesn't inflate a world's total", () => {
      ingest([
        { type: "world_change", world: 420, worldTypes: [], ts: "2026-01-01T00:00:00.000Z" },
        { type: "world_change", world: 421, worldTypes: [], ts: "2026-01-02T00:00:00.000Z" }, // 24h later
      ]);
      const breakdown = getWorldBreakdown(db, playerId, {});
      const w420 = breakdown.find((w) => w.world === 420);
      expect(w420?.minutes).toBe(360); // capped at 6h = 360m, not 24h = 1440m
    });

    it("parses stored world_types JSON back into an array", () => {
      ingest([
        { type: "world_change", world: 421, worldTypes: ["MEMBERS", "PVP"], ts: "2026-01-01T00:00:00.000Z" },
        { type: "world_change", world: 420, worldTypes: [], ts: "2026-01-01T00:10:00.000Z" },
      ]);
      const breakdown = getWorldBreakdown(db, playerId, {});
      const w421 = breakdown.find((w) => w.world === 421);
      expect(w421?.worldTypes).toEqual(["MEMBERS", "PVP"]);
    });
  });

  describe("getBank", () => {
    it("returns items sorted by value descending with meta", () => {
      ingest([
        {
          type: "bank_snapshot",
          totalValue: 150,
          ts: "2026-01-01T00:00:00.000Z",
          items: [
            { itemId: 1, itemName: "Bones", quantity: 1, value: 5 },
            { itemId: 2, itemName: "Coins", quantity: 100, value: 100 },
            { itemId: 3, itemName: "Rune", quantity: 45, value: 45 },
          ],
        },
      ]);
      const bank = getBank(db, playerId);
      expect(bank.items.map((i) => i.itemName)).toEqual(["Coins", "Rune", "Bones"]);
      expect(bank.totalValue).toBe(150);
      expect(bank.itemCount).toBe(3);
      expect(bank.lastSyncedAt).toBe("2026-01-01T00:00:00.000Z");
    });

    it("returns an empty bank state for a player with no snapshot yet", () => {
      const bank = getBank(db, playerId);
      expect(bank.items).toEqual([]);
      expect(bank.totalValue).toBe(0);
      expect(bank.lastSyncedAt).toBeNull();
    });
  });

  describe("getDiaryTaskProgress", () => {
    it("groups by area then orders tiers easy-to-elite regardless of insertion order", () => {
      ingest([
        {
          type: "diary_task_progress",
          diaryArea: "ARDOUGNE",
          tier: "ELITE",
          taskName: "An elite task",
          completed: false,
          ts: "2026-01-01T00:00:00.000Z",
        },
        {
          type: "diary_task_progress",
          diaryArea: "ARDOUGNE",
          tier: "EASY",
          taskName: "An easy task",
          completed: true,
          ts: "2026-01-01T00:00:00.000Z",
        },
        {
          type: "diary_task_progress",
          diaryArea: "ARDOUGNE",
          tier: "HARD",
          taskName: "A hard task",
          completed: false,
          ts: "2026-01-01T00:00:00.000Z",
        },
      ]);

      const rows = getDiaryTaskProgress(db, playerId);
      expect(rows.map((r) => r.tier)).toEqual(["EASY", "HARD", "ELITE"]);
      expect(rows[0].completed).toBe(1);
      expect(rows[0].taskName).toBe("An easy task");
    });

    it("returns nothing for a player with no diary pages viewed yet", () => {
      expect(getDiaryTaskProgress(db, playerId)).toEqual([]);
    });
  });
});
