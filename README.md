# OldStats

Tracks an Old School RuneScape player's XP, monster kills/drops, quests,
slayer tasks, farming patches, collection log unlocks, combat achievements,
achievement diaries, clue scrolls, pets, boss personal bests, PvP kills/deaths,
world/session history, net worth and bank contents via a RuneLite plugin,
stores it on a self-hosted server, and displays it in a mobile-first web app
with a weekly "wrap-up" summary.

## Components

- **`plugin/`** — RuneLite plugin (Kotlin). Tracks stats client-side and
  batches them to the server over HTTP.
- **`server/`** — Backend API (TypeScript + Express + SQLite via
  better-sqlite3). Ingests events from the plugin and serves stats to the web
  app.
- **`web/`** — Mobile-first dashboard (React + TypeScript + Vite). Shows
  overview stats, activity history, and a weekly wrap-up.

## Quick start

A `flake.nix` at the repo root provides Node.js, JDK 17 and Gradle for all
three subprojects — run `nix develop` from the repo root (or any subproject)
to get a shell with everything needed.

### 1. Server

```
cd server
cp .env.example .env   # adjust if needed
npm install
npm run dev            # http://localhost:4000
```

Register a player and grab its API key:

```
curl -X POST http://localhost:4000/api/players \
  -H 'Content-Type: application/json' \
  -d '{"username":"your_rsn"}'
```

Save the returned `apiKey` — it's only ever shown once.

### 2. Plugin

```
cd plugin
./gradlew build         # or: nix develop -c gradle build
```

Load the built plugin into RuneLite in developer mode (see RuneLite's
[plugin development docs](https://github.com/runelite/runelite/wiki/Developing-Plugins)),
then in the OldStats plugin config panel set:

- **Server URL** — e.g. `http://localhost:4000`
- **API key** — the key from step 1

Stats are queued in memory and flushed to the server on the configured
interval (default 30s). The queue is in-process only — it does not persist
across a plugin reload while the server is unreachable.

### 3. Web app

```
cd web
cp .env.example .env    # set VITE_API_URL to your server
npm install
npm run dev             # http://localhost:5173
```

Open it on your phone (or resize your browser) — layout is mobile-first with
a bottom tab bar for Dashboard / Wrap-up / Activity / Bank / Player.

### 4. E2E tests

```
cd web
npm run test:e2e        # headless run
npm run test:e2e:ui     # interactive Playwright UI
```

This is a real integration suite, not a mocked-frontend one: `npm run test:e2e`
spins up an actual server instance (`server/`) against a throwaway temp
SQLite DB on port 4500, seeds it via the real HTTP ingest API with a fixed
dataset (`web/e2e/seed-data.ts` — the single source of truth for both the
seed and the assertions), starts the Vite dev server on port 5500 pointed at
it, then runs Playwright against the whole stack. Both processes are torn
down and the temp DB deleted after the run, pass or fail.

Playwright is pinned to `1.61.1` in `package.json` to match the exact browser
build `nix develop` provides via nixpkgs' `playwright-driver.browsers`
(wired up in `flake.nix`) — NixOS can't run Playwright's own downloaded
generic-glibc Chromium build (missing shared libs), and the driver/browser
versions have to match exactly or the CDP handshake fails. If you're not on
NixOS, delete the `PLAYWRIGHT_BROWSERS_PATH`/`PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS`
exports from the flake's `shellHook` and run `npx playwright install
chromium` once instead — any reasonably recent `@playwright/test` will do.

## Notes and known limitations

- **Self-hosted, single/small group use.** The web app's read endpoints are
  unauthenticated by design (`GET /api/players/*`) — this is meant to run on
  a private network or behind your own auth/reverse proxy, not exposed
  publicly. Writing new stats requires the per-player API key
  (`POST /api/events`).
- **Slayer tasks are tracked the same way RuneLite's own built-in Slayer
  plugin does** — no chat parsing. The current task's monster identity comes
  from Jagex's client-side DB table system
  (`Client.getDBRowsByValue`/`getDBTableField`), keyed by
  `VarPlayer.SLAYER_TASK_CREATURE` (with a wilderness/Krystilia special case
  via `VarbitID.SLAYER_TARGET_BOSSID`), and remaining count from
  `VarPlayer.SLAYER_TASK_SIZE`. A task is reported complete when the count
  hits 0 before the next task is assigned; cancelling/skipping a task before
  finishing it does not emit a completion. Points earned are a delta of
  `VarbitID.SLAYER_POINTS` across the task; the `streak` field is left unset
  since the true reward-streak varp (as opposed to a lifetime
  tasks-completed counter) wasn't confidently identified.
- **Farming patch tracking is now location-specific**, matching RuneLite's
  own Farming Tracker (part of the Time Tracking plugin) rather than
  guessing from chat. Since that plugin's actual database
  (`FarmingWorld`/`FarmingPatch`/`PatchImplementation`) is package-private
  and lives in a different plugin's Guice scope, this ports a hand-transcribed
  copy of it instead — see `plugin/.../tracking/farming/`:
  `FarmingWorldData.kt` (all ~43 known regions, their patch names and varbit
  IDs) and `PatchDecode.kt` (the per-crop-type varbit → produce/state decode
  tables for all 23 patch types, dropping only the growth-tick sub-stage
  since this plugin tracks activity, not timers). Both are covered by unit
  tests in `FarmingDataTest.kt`. A patch's varbit is only meaningful while
  you're in/near its region (the same varbit id is reused across many
  unrelated regions), so, like the real plugin, this only reads patches for
  whichever region you're currently standing in — it won't detect a crop
  finishing while you're away, only the state it's in next time you're back.
- **Boss detection** uses a hardcoded name list (`plugin/.../BossList.kt`) —
  extend it if a boss you fight isn't being flagged.
- **Quest completion** has no dedicated RuneLite event either; the plugin
  polls all `Quest` states on `VarbitChanged` and diffs against the last seen
  state. This is cheap (~200 enum reads) but only fires as fast as varbits
  change.
- **Collection log, combat achievements and achievement diaries** also have
  no dedicated RuneLite events, but unlike slayer/farming these are tracked
  via reliable varbit/varp polling (same diff-on-`VarbitChanged` pattern as
  quests), not chat parsing:
  - Collection log unlocks come from the varps backing the game's own "New
    item added to your collection log" popup
    (`VarPlayerID.COLLECTION_OVERVIEW_LAST_ITEM0`), so they're accurate and
    fire even if the log interface is never opened.
  - Combat achievement completions come from ~400 `CA_TASK_*_COMPLETED`
    varbits, found via reflection over `VarbitID` at startup. Task **names
    are derived from the varbit's own constant name** (e.g. "Vorkath
    Killcount 1"), not Jagex's actual task titles — that mapping isn't
    exposed anywhere in the client API, and isn't reliably reconstructable
    from the OSRS Wiki either: spot-checking Vorkath found 11 named wiki
    tasks against only 10 `CA_TASK_VORKATH_*` varbits, so any hardcoded
    varbit-to-wiki-name mapping risks silently mislabeling tasks with no way
    to verify it against a live client. Total CA points are accurate
    (`VarbitID.CA_POINTS`).
  - Achievement diary tiers come from `{AREA}_DIARY_{TIER}_COMPLETE`
    varbits, also found via reflection. Karamja predates the tiered diary
    system and only exposes an `ELITE_COMPLETE` varbit for its own
    already-tiered-at-creation reward track; its original easy/medium/hard
    task lists are tracked via `KARAMJA_EASY_COUNT`/`_MED_COUNT`/`_HARD_COUNT`
    progress varbits instead. The plugin treats those as complete at
    10/19/10 tasks respectively — task totals per tier confirmed against the
    OSRS Wiki's Karamja Diary page, not something the game exposes as a
    ready-made threshold, so this would need updating if Jagex ever
    adds/removes a Karamja diary task.
- **Clue scrolls** use the exact regex RuneLite's own Loot Tracker plugin
  matches completions against (`CLUE_SCROLL_PATTERN` in
  `LootTrackerPlugin`), extended with our own capture group for the count.
  Reliable; doesn't track itemized casket rewards, only the completion
  event (tier + running total).
- **Pets** have no dedicated event and the drop chat message never names the
  pet. The plugin watches for the two known "received a pet" messages, then
  attributes the name to whichever NPC spawns next to the player within a
  few ticks (covers the common auto-follow case). A pet received straight to
  the inventory instead (already have a follower, or no space) is recorded
  as "Unknown pet" — the event count is still accurate, just not the name.
- **Boss personal bests** use the exact regex RuneLite's `ChatCommandsPlugin`
  matches new-PB messages against (`NEW_PB_PATTERN`), which requires the
  player to have the in-game **"Fight duration" chat setting enabled** —
  same requirement as the real plugin. That message never names the boss
  either, so it's attributed to whichever boss the kill tracker most
  recently confirmed a kill for (within 10 seconds); activities without a
  corresponding NPC kill (raids, agility laps, Hallowed Sepulchre, etc.)
  aren't attributed and are silently skipped.
- **PvP kills** use `PlayerLootReceived` — the same reliable core
  LootManager mechanism as monster kills. **Own deaths** use `ActorDeath`
  filtered to the local player; value lost on death is not computed (kept
  items via Protect Item, skull status, etc. make an accurate figure
  nontrivial), so only the death event itself is recorded.
- **World tracking** emits an event whenever the current world changes
  (login, hop, or the very first sighting each session). The server derives
  "time spent per world" from the gap between consecutive events, capping
  any single gap at 6 hours so a forgotten/offline session doesn't inflate
  one world's total indefinitely — this is an approximation, not an exact
  session log.
- **Net worth** is a periodic snapshot (default every 30 minutes, not
  event-driven) of inventory + equipment + bank value via `ItemManager`
  pricing. Bank value is only accurate once the player has opened their bank
  at least once that session — that's when the client actually receives
  bank contents from the server, a RuneLite-wide limitation rather than
  something specific to this plugin.
- **Bank view** shows the player's current bank contents (the web app's
  Bank tab), not a history of past bank compositions — each new snapshot
  wholesale replaces the previous one in the database, mirroring how a bank
  actually works (there's no meaningful "diff" between two bank states to
  track as separate events, unlike kills/drops/etc). Same bank-must-be-opened
  limitation as net worth above. Snapshots are rate-limited to at most once
  per 10 seconds so reorganizing a bank doesn't spam a full item-list
  payload on every slot change.
- **EHP/EHB (efficient hours played/bossed) is intentionally not
  implemented.** It requires community-maintained xp-per-hour and
  kills-per-hour rate tables (hundreds of entries, e.g. what Wise Old Man
  publishes) that aren't exposed anywhere in the client API — fabricating
  those numbers would be worse than not having the feature. Could be added
  later by sourcing a real rate table and computing it entirely server-side
  from XP/kill data this project already collects.
