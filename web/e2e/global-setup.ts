import { spawn } from "node:child_process";
import { existsSync, mkdirSync, rmSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { PLAYER_USERNAME, buildSeedEvents } from "./seed-data.ts";

const __dirname = dirname(fileURLToPath(import.meta.url));
const SERVER_DIR = resolve(__dirname, "../../server");
const TMP_DIR = resolve(__dirname, ".tmp");
const DB_PATH = resolve(TMP_DIR, "e2e.db");
const INFO_PATH = resolve(TMP_DIR, "server-info.json");

export const API_PORT = 4500;
export const API_BASE_URL = `http://localhost:${API_PORT}`;

async function waitForHealth(timeoutMs = 20000): Promise<void> {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    try {
      const res = await fetch(`${API_BASE_URL}/health`);
      if (res.ok) return;
    } catch {
      // server not up yet
    }
    await new Promise((r) => setTimeout(r, 250));
  }
  throw new Error(`Server at ${API_BASE_URL} did not become healthy within ${timeoutMs}ms`);
}

export default async function globalSetup() {
  mkdirSync(TMP_DIR, { recursive: true });
  rmSync(DB_PATH, { force: true });
  rmSync(`${DB_PATH}-shm`, { force: true });
  rmSync(`${DB_PATH}-wal`, { force: true });

  const tsxBin = resolve(SERVER_DIR, "node_modules/.bin/tsx");
  if (!existsSync(tsxBin)) {
    throw new Error(`tsx not found at ${tsxBin} — run "npm install" in server/ first`);
  }

  const child = spawn(tsxBin, ["src/index.ts"], {
    cwd: SERVER_DIR,
    env: { ...process.env, PORT: String(API_PORT), OLDSTATS_DB_PATH: DB_PATH },
    stdio: "ignore",
    detached: true,
  });
  child.unref();

  writeFileSync(INFO_PATH, JSON.stringify({ pid: child.pid, dbPath: DB_PATH }));

  await waitForHealth();

  const registerRes = await fetch(`${API_BASE_URL}/api/players`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: PLAYER_USERNAME }),
  });
  if (!registerRes.ok) {
    throw new Error(`Failed to register e2e player: ${registerRes.status} ${await registerRes.text()}`);
  }
  const { apiKey } = (await registerRes.json()) as { apiKey: string };

  const eventsRes = await fetch(`${API_BASE_URL}/api/events`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${apiKey}` },
    body: JSON.stringify({ events: buildSeedEvents() }),
  });
  if (!eventsRes.ok) {
    throw new Error(`Failed to seed e2e events: ${eventsRes.status} ${await eventsRes.text()}`);
  }
}
