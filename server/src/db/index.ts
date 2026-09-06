import Database from "better-sqlite3";
import { readFileSync, mkdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const __dirname = dirname(fileURLToPath(import.meta.url));

const DB_PATH = process.env.OLDSTATS_DB_PATH ?? join(process.cwd(), "data", "oldstats.db");

export function openDatabase(path: string = DB_PATH): Database.Database {
  mkdirSync(dirname(path), { recursive: true });
  const db = new Database(path);
  db.pragma("journal_mode = WAL");
  db.pragma("foreign_keys = ON");
  const schema = readFileSync(join(__dirname, "schema.sql"), "utf-8");
  db.exec(schema);
  return db;
}

export const db = openDatabase();
