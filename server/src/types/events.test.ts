import { describe, expect, it } from "vitest";
import { ingestEvent, ingestBatch } from "./events.js";

describe("ingestEvent schema", () => {
  it("accepts a valid xp_gain event", () => {
    const result = ingestEvent.safeParse({
      type: "xp_gain",
      skill: "SLAYER",
      xp: 1000,
      xpGained: 100,
      level: 50,
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(true);
  });

  it("rejects an unknown event type", () => {
    const result = ingestEvent.safeParse({ type: "not_a_real_event", ts: "2026-01-01T00:00:00.000Z" });
    expect(result.success).toBe(false);
  });

  it("rejects xp_gain missing required fields", () => {
    const result = ingestEvent.safeParse({ type: "xp_gain", skill: "SLAYER" });
    expect(result.success).toBe(false);
  });

  it("defaults drop quantity and value when omitted", () => {
    const result = ingestEvent.safeParse({
      type: "drop",
      itemName: "Bones",
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(true);
    if (result.success && result.data.type === "drop") {
      expect(result.data.quantity).toBe(1);
      expect(result.data.value).toBe(0);
    }
  });

  it("rejects a bank_snapshot with more than 2000 items", () => {
    const items = Array.from({ length: 2001 }, (_, i) => ({
      itemId: i,
      itemName: `Item ${i}`,
      quantity: 1,
      value: 1,
    }));
    const result = ingestEvent.safeParse({
      type: "bank_snapshot",
      items,
      totalValue: 2001,
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(false);
  });

  it("accepts a bank_snapshot with an empty item list", () => {
    const result = ingestEvent.safeParse({
      type: "bank_snapshot",
      items: [],
      totalValue: 0,
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(true);
  });

  it("rejects a personal_best with a negative duration", () => {
    const result = ingestEvent.safeParse({
      type: "personal_best",
      activityName: "Vorkath",
      durationSeconds: -1,
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(false);
  });

  it("rejects a diary_completed with an invalid tier", () => {
    const result = ingestEvent.safeParse({
      type: "diary_completed",
      diaryArea: "ARDOUGNE",
      tier: "IMPOSSIBLE",
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(false);
  });

  it("accepts a valid diary_task_progress", () => {
    const result = ingestEvent.safeParse({
      type: "diary_task_progress",
      diaryArea: "ARDOUGNE",
      tier: "EASY",
      taskName: "Enter the Wilderness",
      completed: true,
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(true);
  });

  it("rejects a diary_task_progress with an invalid tier", () => {
    const result = ingestEvent.safeParse({
      type: "diary_task_progress",
      diaryArea: "ARDOUGNE",
      tier: "IMPOSSIBLE",
      taskName: "Enter the Wilderness",
      completed: true,
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(false);
  });

  it("rejects a diary_task_progress missing completed", () => {
    const result = ingestEvent.safeParse({
      type: "diary_task_progress",
      diaryArea: "ARDOUGNE",
      tier: "EASY",
      taskName: "Enter the Wilderness",
      ts: "2026-01-01T00:00:00.000Z",
    });
    expect(result.success).toBe(false);
  });
});

describe("ingestBatch schema", () => {
  it("rejects an empty events array", () => {
    const result = ingestBatch.safeParse({ events: [] });
    expect(result.success).toBe(false);
  });

  it("rejects more than 1000 events", () => {
    const events = Array.from({ length: 1001 }, () => ({
      type: "pet_received" as const,
      ts: "2026-01-01T00:00:00.000Z",
    }));
    const result = ingestBatch.safeParse({ events });
    expect(result.success).toBe(false);
  });

  it("accepts a mixed batch of different event types", () => {
    const result = ingestBatch.safeParse({
      events: [
        { type: "pet_received", petName: "Vorki", ts: "2026-01-01T00:00:00.000Z" },
        { type: "clue_completed", tier: "Elite", count: 5, ts: "2026-01-01T00:00:00.000Z" },
      ],
    });
    expect(result.success).toBe(true);
  });
});
