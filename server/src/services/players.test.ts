import { beforeEach, describe, expect, it } from "vitest";
import type { Database } from "better-sqlite3";
import { freshDb } from "../test-helpers.js";
import { createPlayer, getPlayerByApiKey, getPlayerByUsername, getPlayerById, listPlayers } from "./players.js";

describe("players service", () => {
  let db: Database;

  beforeEach(() => {
    db = freshDb();
  });

  it("creates a player with a unique generated api key", () => {
    const a = createPlayer(db, "zezima");
    const b = createPlayer(db, "woox");
    expect(a.apiKey).not.toBe(b.apiKey);
    expect(a.apiKey.length).toBeGreaterThan(10);
  });

  it("looks players up by id, username and api key", () => {
    const created = createPlayer(db, "zezima");
    expect(getPlayerById(db, created.id)?.username).toBe("zezima");
    expect(getPlayerByUsername(db, "zezima")?.id).toBe(created.id);
    expect(getPlayerByApiKey(db, created.apiKey)?.id).toBe(created.id);
  });

  it("returns undefined for unknown lookups", () => {
    expect(getPlayerById(db, 999)).toBeUndefined();
    expect(getPlayerByUsername(db, "nobody")).toBeUndefined();
    expect(getPlayerByApiKey(db, "not-a-real-key")).toBeUndefined();
  });

  it("rejects duplicate usernames at the database level", () => {
    createPlayer(db, "zezima");
    expect(() => createPlayer(db, "zezima")).toThrow();
  });

  it("lists players alphabetically by username", () => {
    createPlayer(db, "woox");
    createPlayer(db, "aaron");
    createPlayer(db, "zezima");
    expect(listPlayers(db).map((p) => p.username)).toEqual(["aaron", "woox", "zezima"]);
  });
});
