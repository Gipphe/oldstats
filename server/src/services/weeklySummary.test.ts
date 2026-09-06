import { beforeEach, describe, expect, it } from "vitest";
import type { Database } from "better-sqlite3";
import { freshDb } from "../test-helpers.js";
import { createPlayer } from "./players.js";
import { ingestEvents } from "./ingest.js";
import type { IngestEvent } from "../types/events.js";
import { getWeeklySummary } from "./weeklySummary.js";

const WEEK_START = "2026-01-01T00:00:00.000Z";
const WEEK_END = "2026-01-08T00:00:00.000Z";
const IN_WEEK = "2026-01-03T00:00:00.000Z";
const BEFORE_WEEK = "2025-12-25T00:00:00.000Z";

describe("getWeeklySummary", () => {
  let db: Database;
  let playerId: number;

  beforeEach(() => {
    db = freshDb();
    playerId = createPlayer(db, "zezima").id;
  });

  function ingest(events: IngestEvent[]) {
    ingestEvents(db, playerId, events);
  }

  function summary() {
    return getWeeklySummary(db, playerId, WEEK_START, WEEK_END);
  }

  it("falls back to a friendly message when there is no activity", () => {
    const result = summary();
    expect(result.highlights).toEqual(["No tracked activity this week — time to log back in!"]);
  });

  it("builds a title-cased 'most trained skill' highlight from raw enum names", () => {
    ingest([{ type: "xp_gain", skill: "SLAYER", xp: 5000, xpGained: 5000, level: 80, ts: IN_WEEK }]);
    const result = summary();
    expect(result.totalXpGained).toBe(5000);
    expect(result.highlights).toContain("Gained 5,000 total XP this week.");
    expect(result.highlights).toContain("Most trained skill: Slayer (+5,000 xp).");
  });

  it("only counts events inside the week window", () => {
    ingest([
      { type: "kill", npcName: "Vorkath", isBoss: true, ts: IN_WEEK },
      { type: "kill", npcName: "Vorkath", isBoss: true, ts: BEFORE_WEEK },
    ]);
    const result = summary();
    expect(result.totalKills).toBe(1);
    expect(result.bossKills).toBe(1);
  });

  it("computes combat achievement points earned as a delta across the week boundary", () => {
    // Already had 100 points before the week started...
    ingest([{ type: "combat_achievement", taskName: "Old task", totalPoints: 100, ts: BEFORE_WEEK }]);
    // ...and gains 50 more points during the week.
    ingest([{ type: "combat_achievement", taskName: "New task", totalPoints: 150, ts: IN_WEEK }]);

    const result = summary();
    expect(result.combatAchievementsCompleted).toHaveLength(1);
    expect(result.combatAchievementPointsEarned).toBe(50);
  });

  it("reports combat achievement points earned as the full total when there's no prior baseline", () => {
    ingest([{ type: "combat_achievement", taskName: "First ever task", totalPoints: 30, ts: IN_WEEK }]);
    const result = summary();
    expect(result.combatAchievementPointsEarned).toBe(30);
  });

  it("computes net worth change only when both a before and after snapshot exist", () => {
    ingest([
      {
        type: "net_worth_snapshot",
        inventoryValue: 0,
        equipmentValue: 0,
        bankValue: 0,
        totalValue: 1000,
        ts: BEFORE_WEEK,
      },
      {
        type: "net_worth_snapshot",
        inventoryValue: 0,
        equipmentValue: 0,
        bankValue: 0,
        totalValue: 1500,
        ts: IN_WEEK,
      },
    ]);
    const result = summary();
    expect(result.netWorthChange).toBe(500);
  });

  it("leaves net worth change null when there's no snapshot before the week started", () => {
    ingest([
      {
        type: "net_worth_snapshot",
        inventoryValue: 0,
        equipmentValue: 0,
        bankValue: 0,
        totalValue: 1500,
        ts: IN_WEEK,
      },
    ]);
    const result = summary();
    expect(result.netWorthChange).toBeNull();
  });

  it("picks the world with the most minutes as the top world", () => {
    ingest([
      { type: "world_change", world: 420, worldTypes: [], ts: "2026-01-01T00:00:00.000Z" },
      { type: "world_change", world: 421, worldTypes: [], ts: "2026-01-01T00:10:00.000Z" },
      { type: "world_change", world: 420, worldTypes: [], ts: "2026-01-01T01:10:00.000Z" },
    ]);
    const result = summary();
    expect(result.topWorld?.world).toBe(420);
  });

  it("surfaces PvP, clue, pet and diary highlights", () => {
    ingest([
      { type: "player_kill", opponentName: "Foe", ts: IN_WEEK },
      { type: "player_death", ts: IN_WEEK },
      { type: "clue_completed", tier: "Elite", count: 1, ts: IN_WEEK },
      { type: "pet_received", petName: "Vorki", ts: IN_WEEK },
      { type: "diary_completed", diaryArea: "ARDOUGNE", tier: "ELITE", ts: IN_WEEK },
    ]);
    const result = summary();
    expect(result.highlights).toContain("Defeated 1 player in combat.");
    expect(result.highlights).toContain("Died 1 time.");
    expect(result.highlights).toContain("Completed 1 clue scroll.");
    expect(result.highlights).toContain("Received 1 pet: Vorki.");
    expect(result.highlights).toContain("Completed Ardougne (Elite) diary.");
  });
});
