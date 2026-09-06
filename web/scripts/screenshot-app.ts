/**
 * Screenshots every page/tab of the web app against a freshly seeded server,
 * for dropping into design-review folders after UI changes. Reuses the e2e
 * suite's own seed data and server bootstrapping so the two never drift
 * apart — don't run this at the same time as `npm run test:e2e`, since both
 * use the same throwaway server port and temp DB.
 *
 * Usage: npm run screenshot [-- <output-dir>]
 * Defaults to web/.screenshots/ if no output dir is given.
 */
import { spawn, type ChildProcess } from "node:child_process";
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import globalSetup, { API_BASE_URL } from "../e2e/global-setup.ts";
import globalTeardown from "../e2e/global-teardown.ts";

const __dirname = dirname(fileURLToPath(import.meta.url));
const WEB_DIR = resolve(__dirname, "..");
const VITE_PORT = 5501;
const CDP_PORT = 9333;

const OUT_DIR = resolve(process.cwd(), process.argv[2] ?? process.env.SCREENSHOT_OUT_DIR ?? resolve(WEB_DIR, ".screenshots"));

interface Shot {
  path: string;
  file: string;
  tabText?: string;
}

const SHOTS: Shot[] = [
  { path: "/", file: "01-dashboard.png" },
  { path: "/wrap-up", file: "02-wrap-up.png" },
  { path: "/activity", file: "03-activity-kills.png" },
  { path: "/activity", file: "04-activity-drops.png", tabText: "Drops" },
  { path: "/activity", file: "05-activity-quests.png", tabText: "Quests" },
  { path: "/activity", file: "06-activity-slayer.png", tabText: "Slayer" },
  { path: "/activity", file: "07-activity-farming.png", tabText: "Farming" },
  { path: "/activity", file: "08-activity-collection-log.png", tabText: "Collection log" },
  { path: "/activity", file: "09-activity-combat-achievements.png", tabText: "Combat achievements" },
  { path: "/activity", file: "10-activity-diaries.png", tabText: "Diaries" },
  { path: "/activity", file: "11-activity-clues.png", tabText: "Clues" },
  { path: "/activity", file: "12-activity-pets.png", tabText: "Pets" },
  { path: "/activity", file: "13-activity-personal-bests.png", tabText: "Personal bests" },
  { path: "/activity", file: "14-activity-pvp-kills.png", tabText: "PvP kills" },
  { path: "/activity", file: "15-activity-deaths.png", tabText: "Deaths" },
  { path: "/player", file: "16-player.png" },
  { path: "/bank", file: "17-bank.png" },
];

/**
 * `verify` guards against a stale, unrelated process already squatting on
 * the target port: a plain "did it respond 200" check can't tell that apart
 * from our own server actually being up. Vite's dev CLI also doesn't always
 * exit on a startup error (e.g. EADDRINUSE without --strictPort just falls
 * back to another port; even with --strictPort it can print the error
 * without necessarily terminating right away), so content verification is
 * the reliable signal, not process lifecycle.
 */
function waitForHttp(url: string, timeoutMs = 20000, verify?: (body: string) => boolean): Promise<void> {
  const deadline = Date.now() + timeoutMs;
  return new Promise((resolvePromise, reject) => {
    const tick = async () => {
      try {
        const res = await fetch(url);
        if (res.ok) {
          if (!verify || verify(await res.text())) return resolvePromise();
        }
      } catch {
        // not up yet
      }
      if (Date.now() > deadline) return reject(new Error(`${url} did not respond as expected within ${timeoutMs}ms`));
      setTimeout(tick, 250);
    };
    tick();
  });
}

/**
 * Like waitForHttp, but also races against the process that's supposed to
 * be serving that URL, failing fast if it exits before coming up (e.g. a
 * genuine EADDRINUSE crash) instead of waiting out the full timeout.
 */
function waitForHttpOrExit(url: string, child: ChildProcess, label: string, timeoutMs = 20000, verify?: (body: string) => boolean): Promise<void> {
  return new Promise((resolvePromise, reject) => {
    let settled = false;
    const onExit = (code: number | null, signal: NodeJS.Signals | null) => {
      if (settled) return;
      settled = true;
      reject(new Error(`${label} exited before it came up (code=${code} signal=${signal}) — is something else already using its port?`));
    };
    child.once("exit", onExit);
    waitForHttp(url, timeoutMs, verify).then(
      () => {
        if (settled) return;
        settled = true;
        child.off("exit", onExit);
        resolvePromise();
      },
      (err) => {
        if (settled) return;
        settled = true;
        child.off("exit", onExit);
        reject(err);
      },
    );
  });
}

/**
 * Defaults to the `chromium` on PATH (the flake devShell puts nixpkgs'
 * chromium there). Override with CHROMIUM_BIN if that's not right for your
 * setup — the chrome process's own error output will say why it failed.
 */
function findChromiumBinary(): string {
  return process.env.CHROMIUM_BIN ?? "chromium";
}

let msgId = 0;
function cdpSend(ws: WebSocket, method: string, params: Record<string, unknown> = {}): Promise<any> {
  return new Promise((resolvePromise, reject) => {
    const id = ++msgId;
    const handler = (ev: MessageEvent) => {
      const msg = JSON.parse(ev.data as string);
      if (msg.id === id) {
        ws.removeEventListener("message", handler);
        if (msg.error) reject(new Error(JSON.stringify(msg.error)));
        else resolvePromise(msg.result);
      }
    };
    ws.addEventListener("message", handler);
    ws.send(JSON.stringify({ id, method, params }));
  });
}

function cdpConnect(wsUrl: string): Promise<WebSocket> {
  return new Promise((resolvePromise, reject) => {
    const ws = new WebSocket(wsUrl);
    ws.addEventListener("open", () => resolvePromise(ws));
    ws.addEventListener("error", reject);
  });
}

async function shoot(cdpBase: string, shot: Shot) {
  const tab = await (await fetch(`${cdpBase}/json/new?about:blank`, { method: "PUT" })).json();
  const ws = await cdpConnect(tab.webSocketDebuggerUrl);
  await cdpSend(ws, "Page.enable");
  await cdpSend(ws, "Runtime.enable");
  await cdpSend(ws, "Emulation.setDeviceMetricsOverride", { width: 430, height: 932, deviceScaleFactor: 2, mobile: true });
  await cdpSend(ws, "Page.navigate", { url: `http://localhost:${VITE_PORT}${shot.path}` });
  await new Promise((r) => setTimeout(r, 1300));

  if (shot.tabText) {
    await cdpSend(ws, "Runtime.evaluate", {
      expression: `[...document.querySelectorAll('.tab-button')].find(b => b.textContent.includes(${JSON.stringify(shot.tabText)}))?.click()`,
    });
    await new Promise((r) => setTimeout(r, 500));
  }

  const { result } = await cdpSend(ws, "Runtime.evaluate", { expression: "document.body.scrollHeight" });
  const height = Math.min(Math.ceil(result.value), 6000);
  await cdpSend(ws, "Emulation.setDeviceMetricsOverride", { width: 430, height, deviceScaleFactor: 2, mobile: true });
  await new Promise((r) => setTimeout(r, 300));

  const { data } = await cdpSend(ws, "Page.captureScreenshot", {
    format: "png",
    captureBeyondViewport: true,
    clip: { x: 0, y: 0, width: 430, height, scale: 1 },
  });
  writeFileSync(resolve(OUT_DIR, shot.file), Buffer.from(data, "base64"));
  ws.close();
  await fetch(`${cdpBase}/json/close/${tab.id}`);
  console.log(`  wrote ${shot.file} (${height}px)`);
}

async function main() {
  mkdirSync(OUT_DIR, { recursive: true });

  console.log("Seeding a throwaway server...");
  await globalSetup();
  await waitForHttp(`${API_BASE_URL}/health`);

  console.log("Starting the vite dev server...");
  const vite: ChildProcess = spawn(resolve(WEB_DIR, "node_modules/.bin/vite"), ["--port", String(VITE_PORT), "--strictPort"], {
    cwd: WEB_DIR,
    env: { ...process.env, VITE_API_URL: API_BASE_URL },
    stdio: ["ignore", "ignore", "inherit"],
  });
  await waitForHttpOrExit(`http://localhost:${VITE_PORT}/`, vite, "vite", 20000, (body) => body.includes("<title>OldStats</title>"));

  console.log("Launching headless chromium...");
  const chromeProfileDir = mkdtempSync(resolve(tmpdir(), "oldstats-shot-"));
  const chrome: ChildProcess = spawn(
    findChromiumBinary(),
    [
      "--headless=new",
      "--disable-gpu",
      "--no-sandbox",
      `--remote-debugging-port=${CDP_PORT}`,
      "--window-size=430,932",
      `--user-data-dir=${chromeProfileDir}`,
      "about:blank",
    ],
    { stdio: ["ignore", "ignore", "inherit"] },
  );
  const cdpBase = `http://localhost:${CDP_PORT}`;
  await waitForHttpOrExit(`${cdpBase}/json/version`, chrome, "chromium");

  console.log(`Capturing ${SHOTS.length} screenshots to ${OUT_DIR}...`);
  for (const shot of SHOTS) {
    await shoot(cdpBase, shot);
  }

  console.log("Cleaning up...");
  chrome.kill("SIGKILL");
  vite.kill("SIGKILL");
  rmSync(chromeProfileDir, { recursive: true, force: true });
  await globalTeardown();

  console.log(`Done. Screenshots are in ${OUT_DIR}`);
}

main().catch(async (err) => {
  console.error(err);
  await globalTeardown().catch(() => {});
  process.exit(1);
});
