# OldStats

Tracks an Old School RuneScape player's XP, monster kills/drops, quests,
slayer tasks, farming patches, collection log unlocks, combat achievements,
achievement diaries, clue scrolls, pets, boss personal bests, PvP kills/deaths,
world/session history, net worth and bank contents via a RuneLite plugin,
stores it on a self-hosted server, and displays it in a mobile-first web app
with a weekly "wrap-up" summary.

## Components

- **`plugin/`** — RuneLite plugin (Java). Tracks stats client-side and
  batches them to the server over HTTP.
- **`server/`** — Backend API (TypeScript + Express + SQLite via
  better-sqlite3). Ingests events from the plugin and serves stats to the web
  app.
- **`web/`** — Mobile-first dashboard (React + TypeScript + Vite). Shows
  overview stats, activity history, and a weekly wrap-up.

## Quick start

A `flake.nix` at the repo root provides Node.js, JDK 17 and Gradle for all
three subprojects — run `nix develop` from the repo root (or any subproject)
to get a shell with everything needed. It also packages all three proper:
`packages.<system>.server`/`web` (built with `buildNpmPackage`) are exposed
as flake apps — `nix run .#server` (or just `nix run .`) builds if needed
and starts it, no dev shell, no manual `npm install`, works from anywhere.
`nix build .#server`/`.#web` builds without running
(`result/bin/oldstats-server`/`oldstats-web`). `packages.<system>.plugin`
(built with `gradle_8`, see below) similarly gives you
`nix build .#plugin` → `result/oldstats-plugin.jar`, with no app entry since
a jar isn't directly runnable.

By default the packaged server binary stores its SQLite DB under
`$XDG_DATA_HOME/oldstats/oldstats.db` (falling back to
`~/.local/share/oldstats/oldstats.db`) and listens on port 4000 — override
with `OLDSTATS_DB_PATH`/`PORT` env vars, same as the manual setup below.

The packaged web app (`nix run .#web`) serves the production build via
`vite preview` on port 3000 (kept apart from the server's own default of
4000), with `VITE_API_URL` fixed to
`http://localhost:4000` at build time — Vite bakes `VITE_*` vars into the
compiled JS bundle (`import.meta.env.VITE_API_URL`), so unlike the server
this can't be overridden with an env var at runtime; edit `env.VITE_API_URL`
in `flake.nix` and rebuild if your server lives elsewhere. Extra args (e.g.
`-- --port 4200`) pass through to `vite preview`.

### 1. Server

```
nix run .#server        # http://localhost:4000
```

Or the manual, non-Nix-packaged equivalent — e.g. for running the test suite,
or iterating on the server itself (`nix run` rebuilds from scratch on every
source change, which is fine for actually running it but slow for active
development):

```
cd server
cp .env.example .env    # adjust if needed; not auto-loaded, export manually or `set -a; source .env; set +a`
npm install
npm run dev             # http://localhost:4000
npm test                # unit + supertest integration tests (in-memory SQLite)
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
./gradlew test          # JUnit + Mockito unit tests for every tracker
./gradlew runClient     # launches a real RuneLite client with the plugin preloaded
```

Or, from anywhere, `nix run .#runelite` — a thin wrapper around
`gradle runClient` (using `gradle_8` and your actual checkout, not a Nix
store copy, since this needs a writable Gradle project dir and a real
display, neither of which fit a hermetic build like `packages.plugin`). It
runs with `--no-daemon` so a stale Gradle daemon (e.g. one started without a
display) can never poison a later GUI-launching run with its snapshotted
environment. RuneLite's UI has no native Wayland backend, so on a Wayland
desktop it needs XWayland; if `DISPLAY` isn't already exported (common on
wlroots compositors like Hyprland, which don't always propagate it into
every shell), the wrapper falls back to the first live socket it finds in
`/tmp/.X11-unix/`.

`runClient` is the standard way to try an unpublished plugin without going
through Plugin Hub review — see `OldStatsPluginTest.java`, which just calls
`ExternalPluginManager.loadBuiltin(OldStatsPlugin.class)` before
`RuneLite.main()`. It opens an independent RuneLite client (your regular
client/launcher, if any, is untouched); log in there like normal. In that
client's plugin list, find "OldStats" and set:

- **Server URL** — e.g. `http://localhost:4000`
- **API key** — the key from step 1

Stats are queued in memory and flushed to the server on the configured
interval (default 30s). The queue is in-process only — it does not persist
across a plugin reload while the server is unreachable.

#### Why not sideload it into your existing (launcher-installed) RuneLite?

RuneLite has a sideloading mechanism (`~/.runelite/sideloaded-plugins/`,
gated behind developer mode) that looks like the obvious way to add this to
your normal RuneLite install. It doesn't actually work for that, though —
verified against RuneLite's own source, not just docs:

```java
// runelite-client RuneLite.java
final boolean developerMode = options.has("developer-mode") && RuneLiteProperties.getLauncherVersion() == null;
```

The official Launcher **unconditionally** stamps every client it starts with
a version marker (`Launcher.java`: `jvmProps.put(LauncherProperties.getVersionKey(), ...)`,
key `runelite.launcher.version`) — regardless of platform (exe, Jagex
launcher, AppImage all go through this). So `getLauncherVersion()` is never
`null` when launched via the official launcher, `developerMode` is always
`false`, and the sideloader's own check (`if (!developerMode) return;`)
bails before ever looking in `sideloaded-plugins/` — no matter what you put
in `--configure`'s Client arguments field. Sideloading only works if you run
the bare client JAR directly, bypassing the launcher entirely, which for
most people means building RuneLite from source — at which point you may as
well use `runClient` below instead, since it isn't gated by developer mode
at all.

If you still want the jar for some other reason (e.g. a genuinely
self-built, launcher-free client), it's `./gradlew jar` →
`build/libs/oldstats-plugin-1.0.0.jar`, or `nix build .#plugin` →
`result/oldstats-plugin.jar` (same jar, built hermetically via `gradle_8` +
nixpkgs' Gradle dependency proxy). No fat-jar/shadow plugin needed — the
plugin is pure Java with no extra runtime dependencies beyond what `client`
already provides on RuneLite's own classpath. If `plugin/build.gradle`'s
dependencies ever change, the Nix build will fail with a message that
`plugin/deps.json` is out of date — regenerate it with:

```
nix build .#plugin.mitmCache.updateScript -o /tmp/update-oldstats-plugin-deps
/tmp/update-oldstats-plugin-deps
git add plugin/deps.json
```

### 3. Web app

```
nix run .#web           # http://localhost:3000, points at http://localhost:4000
```

Or the manual, non-Nix-packaged equivalent, needed if your server isn't at
`http://localhost:4000` (see the build-time `VITE_API_URL` note above) or
you're actively developing the web app:

```
cd web
cp .env.example .env    # set VITE_API_URL to your server
npm install
npm run dev             # http://localhost:3000
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

### 5. Screenshots

```
cd web
npm run screenshot -- /path/to/output/dir   # defaults to web/.screenshots/
```

Seeds the same throwaway server + fixed dataset as the e2e suite, starts Vite
against it, drives a headless `chromium` (from PATH — override with
`CHROMIUM_BIN`) over the DevTools protocol, and dumps a full-height PNG of
every page and Activity tab. Don't run it alongside `npm run test:e2e` — both
use the same server port and temp DB. See `web/scripts/screenshot-app.ts`.

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
  `FarmingWorldData.java` (all ~43 known regions, their patch names and varbit
  IDs) and `PatchDecode.java` (the per-crop-type varbit → produce/state decode
  tables for all 23 patch types, dropping only the growth-tick sub-stage
  since this plugin tracks activity, not timers). Both are covered by unit
  tests in `FarmingDataTest.java`. A patch's varbit is only meaningful while
  you're in/near its region (the same varbit id is reused across many
  unrelated regions), so, like the real plugin, this only reads patches for
  whichever region you're currently standing in — it won't detect a crop
  finishing while you're away, only the state it's in next time you're back.
- **Boss detection** uses a hardcoded name list (`plugin/.../BossList.java`) —
  extend it if a boss you fight isn't being flagged.
- **Quest completion** has no dedicated RuneLite event either; the plugin
  polls all `Quest` states on `VarbitChanged` and diffs against the last seen
  state. `Quest.getState()` runs a real CS2 script per quest (not a cheap
  field read), and varbits are often changed by scripts themselves, so this
  is deferred one client tick via `ClientThread` to avoid a
  `scripts are not reentrant` crash — see `QuestTracker.java`.
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
  - **Individual diary task progress** (which specific tasks within a tier
    are done, not just whether the whole tier is complete) has no obviously
    diary-named varbit, but two independent trackers cover it from different
    angles, both feeding the same `diary_task_progress` event:
    - [`DiaryBitsetTracker`](plugin/src/main/java/com/oldstats/tracking/DiaryBitsetTracker.java)
      is the primary, always-on one. Individual tasks turn out to be tracked
      as single bits packed into a couple of `VarPlayer`s per area (e.g.
      `ARDOUNGE_ACHIEVEMENT_DIARY`/`_DIARY2`) — the same mechanism RuneLite's
      own **Quest Helper** plugin uses to decide which diary steps to skip.
      [`DiaryTaskData.java`](plugin/src/main/java/com/oldstats/tracking/diary/DiaryTaskData.java)
      is a ~450-entry table (area, tier, task name, varp, bit position) ported
      by fetching and parsing all 48 of Quest Helper's own
      `helpers/achievementdiaries/**` source files (github.com/Zoinkwiz/quest-helper)
      directly, not reconstructed from memory — every `VarPlayerID` constant
      referenced was cross-checked against this project's own pinned RuneLite
      API jar and compiles clean. Task **names** are Quest Helper's own short
      editorial labels where a file bothered to set one, and its own terse
      internal variable names (lightly expanded for common OSRS abbreviations
      like "TP"/"Agi"/"Mith") everywhere else — only one of the 48 source
      files actually set nicer labels, so most names are readably terse
      rather than verbatim in-game wording. Doesn't cover Karamja's
      easy/medium/hard tiers, which predate this bitset system entirely (see
      above) and have no per-task identity in the game's state at all, bitset
      or otherwise.
    - [`DiaryTaskTracker`](plugin/src/main/java/com/oldstats/tracking/DiaryTaskTracker.java)
      is a supplementary, opportunistic tracker reading the diary journal's
      own widget tree — the game renders each task line and wraps
      already-completed ones in `<str>` (strikethrough) tags, the same signal
      RuneLite's bundled "Diary Requirements" plugin keys off of (confirmed
      by decompiling it). Only updates when the player actually opens that
      area's diary page in-game, same "can't be polled" limitation as the
      bank tracker — but unlike the bitset tracker, it *does* reach Karamja's
      legacy tiers (the game evidently still renders per-task strikethrough
      state for them even without a discrete bit backing it), and its task
      names come from the journal's real in-game text rather than Quest
      Helper's shorthand. The two trackers' rows simply upsert independently
      into the same table; neither depends on the other.
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
