import { existsSync, readFileSync, rmSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const TMP_DIR = resolve(__dirname, ".tmp");
const INFO_PATH = resolve(TMP_DIR, "server-info.json");

export default async function globalTeardown() {
  if (!existsSync(INFO_PATH)) return;

  const { pid, dbPath } = JSON.parse(readFileSync(INFO_PATH, "utf-8")) as { pid: number; dbPath: string };

  try {
    // Negative pid targets the whole detached process group, in case tsx
    // spawned a nested node process rather than exec-ing in place.
    process.kill(-pid, "SIGTERM");
  } catch {
    try {
      process.kill(pid, "SIGTERM");
    } catch {
      // already gone
    }
  }

  rmSync(dbPath, { force: true });
  rmSync(`${dbPath}-shm`, { force: true });
  rmSync(`${dbPath}-wal`, { force: true });
  rmSync(TMP_DIR, { recursive: true, force: true });
}
