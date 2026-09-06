import { beforeEach, describe, expect, it } from "vitest";
import type { Database } from "better-sqlite3";
import { freshDb } from "../test-helpers.js";
import { createPlayer } from "./players.js";
import { ingestEvents } from "./ingest.js";
import type { IngestEvent } from "../types/events.js";

describe("ingestEvents", () => {
  let db: Database;
  let playerId: number;

  beforeEach(() => {
    db = freshDb();
    playerId = createPlayer(db, "zezima").id;
  });

  function ingest(events: IngestEvent[]) {
    ingestEvents(db, playerId, events);
  }

  it("inserts a plain event like xp_gain as a new row every time", () => {
    ingest([
      { type: "xp_gain", skill: "SLAYER", xp: 100, xpGained: 100, level: 10, ts: "2026-01-01T00:00:00.000Z" },
      { type: "xp_gain", skill: "SLAYER", xp: 200, xpGained: 100, level: 10, ts: "2026-01-01T00:01:00.000Z" },
    ]);
    const count = db.prepare("SELECT COUNT(*) as c FROM xp_gains WHERE player_id = ?").get(playerId) as { c: number };
    expect(count.c).toBe(2);
  });

  it("upserts quests, keyed by (player, quest, state)", () => {
    ingest([
      { type: "quest", questName: "COOKS_ASSISTANT", state: "COMPLETED", questPoints: 1, ts: "2026-01-01T00:00:00.000Z" },
      { type: "quest", questName: "COOKS_ASSISTANT", state: "COMPLETED", questPoints: 1, ts: "2026-01-02T00:00:00.000Z" },
    ]);
    const rows = db.prepare("SELECT * FROM quests WHERE player_id = ?").all(playerId) as { ts: string }[];
    expect(rows).toHaveLength(1);
    expect(rows[0].ts).toBe("2026-01-02T00:00:00.000Z");
  });

  it("only updates a personal best when the new duration is actually better", () => {
    ingest([{ type: "personal_best", activityName: "Vorkath", durationSeconds: 100, ts: "2026-01-01T00:00:00.000Z" }]);
    // Worse time — should be ignored.
    ingest([{ type: "personal_best", activityName: "Vorkath", durationSeconds: 150, ts: "2026-01-02T00:00:00.000Z" }]);
    let row = db.prepare("SELECT * FROM personal_bests WHERE player_id = ?").get(playerId) as {
      duration_seconds: number;
      ts: string;
    };
    expect(row.duration_seconds).toBe(100);
    expect(row.ts).toBe("2026-01-01T00:00:00.000Z");

    // Better time — should update.
    ingest([{ type: "personal_best", activityName: "Vorkath", durationSeconds: 75, ts: "2026-01-03T00:00:00.000Z" }]);
    row = db.prepare("SELECT * FROM personal_bests WHERE player_id = ?").get(playerId) as {
      duration_seconds: number;
      ts: string;
    };
    expect(row.duration_seconds).toBe(75);
    expect(row.ts).toBe("2026-01-03T00:00:00.000Z");
  });

  it("tracks personal bests per activity independently", () => {
    ingest([
      { type: "personal_best", activityName: "Vorkath", durationSeconds: 75, ts: "2026-01-01T00:00:00.000Z" },
      { type: "personal_best", activityName: "Zulrah", durationSeconds: 90, ts: "2026-01-01T00:00:00.000Z" },
    ]);
    const rows = db.prepare("SELECT activity_name FROM personal_bests WHERE player_id = ? ORDER BY activity_name").all(
      playerId,
    ) as { activity_name: string }[];
    expect(rows.map((r) => r.activity_name)).toEqual(["Vorkath", "Zulrah"]);
  });

  it("replaces the entire bank wholesale on each new snapshot", () => {
    ingest([
      {
        type: "bank_snapshot",
        totalValue: 100,
        ts: "2026-01-01T00:00:00.000Z",
        items: [
          { itemId: 1, itemName: "Coins", quantity: 100, value: 100 },
          { itemId: 2, itemName: "Bones", quantity: 1, value: 5 },
        ],
      },
    ]);
    let items = db.prepare("SELECT item_name FROM bank_items WHERE player_id = ?").all(playerId) as {
      item_name: string;
    }[];
    expect(items.map((i) => i.item_name).sort()).toEqual(["Bones", "Coins"]);

    ingest([
      {
        type: "bank_snapshot",
        totalValue: 50,
        ts: "2026-01-02T00:00:00.000Z",
        items: [{ itemId: 3, itemName: "Shark", quantity: 10, value: 50 }],
      },
    ]);
    items = db.prepare("SELECT item_name FROM bank_items WHERE player_id = ?").all(playerId) as { item_name: string }[];
    expect(items.map((i) => i.item_name)).toEqual(["Shark"]);

    const meta = db.prepare("SELECT total_value, item_count FROM bank_snapshots_meta WHERE player_id = ?").get(
      playerId,
    ) as { total_value: number; item_count: number };
    expect(meta.total_value).toBe(50);
    expect(meta.item_count).toBe(1);
  });

  it("upserts collection log items, updating totals in place", () => {
    ingest([
      {
        type: "collection_log_item",
        itemName: "Draconic visage",
        itemId: 21295,
        totalUnlocked: 45,
        totalPossible: 1600,
        ts: "2026-01-01T00:00:00.000Z",
      },
    ]);
    ingest([
      {
        type: "collection_log_item",
        itemName: "Draconic visage",
        itemId: 21295,
        totalUnlocked: 46,
        totalPossible: 1600,
        ts: "2026-01-02T00:00:00.000Z",
      },
    ]);
    const rows = db.prepare("SELECT * FROM collection_log_items WHERE player_id = ?").all(playerId) as {
      total_unlocked: number;
    }[];
    expect(rows).toHaveLength(1);
    expect(rows[0].total_unlocked).toBe(46);
  });

  it("upserts diary task progress, keyed by (player, area, tier, task), flipping completed in place", () => {
    ingest([
      {
        type: "diary_task_progress",
        diaryArea: "ARDOUGNE",
        tier: "EASY",
        taskName: "Enter the Wilderness",
        completed: false,
        ts: "2026-01-01T00:00:00.000Z",
      },
    ]);
    ingest([
      {
        type: "diary_task_progress",
        diaryArea: "ARDOUGNE",
        tier: "EASY",
        taskName: "Enter the Wilderness",
        completed: true,
        ts: "2026-01-02T00:00:00.000Z",
      },
    ]);
    const rows = db.prepare("SELECT * FROM diary_task_progress WHERE player_id = ?").all(playerId) as {
      completed: number;
      ts: string;
    }[];
    expect(rows).toHaveLength(1);
    expect(rows[0].completed).toBe(1);
    expect(rows[0].ts).toBe("2026-01-02T00:00:00.000Z");
  });

  it("tracks diary tasks independently per area and tier", () => {
    ingest([
      {
        type: "diary_task_progress",
        diaryArea: "ARDOUGNE",
        tier: "EASY",
        taskName: "Enter the Wilderness",
        completed: true,
        ts: "2026-01-01T00:00:00.000Z",
      },
      {
        type: "diary_task_progress",
        diaryArea: "ARDOUGNE",
        tier: "MEDIUM",
        taskName: "Steal from the Ardougne market stalls",
        completed: false,
        ts: "2026-01-01T00:00:00.000Z",
      },
    ]);
    const rows = db.prepare("SELECT tier FROM diary_task_progress WHERE player_id = ? ORDER BY tier").all(
      playerId,
    ) as { tier: string }[];
    expect(rows.map((r) => r.tier)).toEqual(["EASY", "MEDIUM"]);
  });

  it("applies an entire batch atomically", () => {
    ingest([
      { type: "clue_completed", tier: "Elite", count: 1, ts: "2026-01-01T00:00:00.000Z" },
      { type: "pet_received", petName: "Vorki", ts: "2026-01-01T00:00:00.000Z" },
      { type: "player_kill", opponentName: "Some Pker", ts: "2026-01-01T00:00:00.000Z" },
    ]);
    expect((db.prepare("SELECT COUNT(*) as c FROM clue_completions").get() as { c: number }).c).toBe(1);
    expect((db.prepare("SELECT COUNT(*) as c FROM pet_drops").get() as { c: number }).c).toBe(1);
    expect((db.prepare("SELECT COUNT(*) as c FROM player_kills").get() as { c: number }).c).toBe(1);
  });
});
