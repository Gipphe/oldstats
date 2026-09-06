import { randomBytes } from "node:crypto";
import type { Database } from "better-sqlite3";

export interface Player {
  id: number;
  username: string;
  apiKey: string;
  accountHash: string | null;
  createdAt: string;
}

function generateApiKey(): string {
  return randomBytes(24).toString("base64url");
}

export function createPlayer(db: Database, username: string, accountHash?: string): Player {
  const apiKey = generateApiKey();
  const stmt = db.prepare(
    `INSERT INTO players (username, api_key, account_hash) VALUES (?, ?, ?)`
  );
  const info = stmt.run(username, apiKey, accountHash ?? null);
  return getPlayerById(db, Number(info.lastInsertRowid))!;
}

function rowToPlayer(row: any): Player {
  return {
    id: row.id,
    username: row.username,
    apiKey: row.api_key,
    accountHash: row.account_hash,
    createdAt: row.created_at,
  };
}

export function getPlayerById(db: Database, id: number): Player | undefined {
  const row = db.prepare(`SELECT * FROM players WHERE id = ?`).get(id) as any;
  return row ? rowToPlayer(row) : undefined;
}

export function getPlayerByUsername(db: Database, username: string): Player | undefined {
  const row = db.prepare(`SELECT * FROM players WHERE username = ?`).get(username) as any;
  return row ? rowToPlayer(row) : undefined;
}

export function getPlayerByApiKey(db: Database, apiKey: string): Player | undefined {
  const row = db.prepare(`SELECT * FROM players WHERE api_key = ?`).get(apiKey) as any;
  return row ? rowToPlayer(row) : undefined;
}

export function listPlayers(db: Database): Player[] {
  const rows = db.prepare(`SELECT * FROM players ORDER BY username`).all() as any[];
  return rows.map(rowToPlayer);
}
