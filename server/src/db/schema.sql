-- OldStats database schema (SQLite)

CREATE TABLE IF NOT EXISTS players (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  api_key TEXT NOT NULL UNIQUE,
  account_hash TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now'))
);

CREATE TABLE IF NOT EXISTS xp_gains (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  skill TEXT NOT NULL,
  xp INTEGER NOT NULL,
  xp_gained INTEGER NOT NULL,
  level INTEGER NOT NULL,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_xp_gains_player_ts ON xp_gains(player_id, ts);
CREATE INDEX IF NOT EXISTS idx_xp_gains_player_skill ON xp_gains(player_id, skill);

CREATE TABLE IF NOT EXISTS level_ups (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  skill TEXT NOT NULL,
  level INTEGER NOT NULL,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_level_ups_player_ts ON level_ups(player_id, ts);

CREATE TABLE IF NOT EXISTS kills (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  npc_name TEXT NOT NULL,
  npc_id INTEGER,
  is_boss INTEGER NOT NULL DEFAULT 0,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_kills_player_ts ON kills(player_id, ts);
CREATE INDEX IF NOT EXISTS idx_kills_player_npc ON kills(player_id, npc_name);

CREATE TABLE IF NOT EXISTS drops (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  npc_name TEXT,
  item_name TEXT NOT NULL,
  item_id INTEGER,
  quantity INTEGER NOT NULL DEFAULT 1,
  value INTEGER NOT NULL DEFAULT 0,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_drops_player_ts ON drops(player_id, ts);
CREATE INDEX IF NOT EXISTS idx_drops_player_value ON drops(player_id, value);

CREATE TABLE IF NOT EXISTS quests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  quest_name TEXT NOT NULL,
  state TEXT NOT NULL,
  quest_points INTEGER,
  ts TEXT NOT NULL,
  UNIQUE(player_id, quest_name, state)
);
CREATE INDEX IF NOT EXISTS idx_quests_player_ts ON quests(player_id, ts);

CREATE TABLE IF NOT EXISTS slayer_tasks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  task_name TEXT NOT NULL,
  amount_assigned INTEGER,
  points INTEGER,
  streak INTEGER,
  started_at TEXT,
  completed_at TEXT
);
CREATE INDEX IF NOT EXISTS idx_slayer_player_completed ON slayer_tasks(player_id, completed_at);

CREATE TABLE IF NOT EXISTS farming_patches (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  patch_name TEXT NOT NULL,
  crop TEXT,
  state TEXT NOT NULL,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_farming_player_ts ON farming_patches(player_id, ts);

CREATE TABLE IF NOT EXISTS collection_log_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  item_name TEXT NOT NULL,
  item_id INTEGER,
  total_unlocked INTEGER,
  total_possible INTEGER,
  ts TEXT NOT NULL,
  UNIQUE(player_id, item_name)
);
CREATE INDEX IF NOT EXISTS idx_collection_log_player_ts ON collection_log_items(player_id, ts);

CREATE TABLE IF NOT EXISTS combat_achievements (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  task_name TEXT NOT NULL,
  total_points INTEGER,
  ts TEXT NOT NULL,
  UNIQUE(player_id, task_name)
);
CREATE INDEX IF NOT EXISTS idx_combat_achievements_player_ts ON combat_achievements(player_id, ts);

CREATE TABLE IF NOT EXISTS achievement_diaries (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  diary_area TEXT NOT NULL,
  tier TEXT NOT NULL,
  ts TEXT NOT NULL,
  UNIQUE(player_id, diary_area, tier)
);
CREATE INDEX IF NOT EXISTS idx_diaries_player_ts ON achievement_diaries(player_id, ts);

CREATE TABLE IF NOT EXISTS clue_completions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  tier TEXT NOT NULL,
  count INTEGER,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_clues_player_ts ON clue_completions(player_id, ts);

CREATE TABLE IF NOT EXISTS pet_drops (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  pet_name TEXT,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_pets_player_ts ON pet_drops(player_id, ts);

CREATE TABLE IF NOT EXISTS personal_bests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  activity_name TEXT NOT NULL,
  duration_seconds REAL NOT NULL,
  ts TEXT NOT NULL,
  UNIQUE(player_id, activity_name)
);
CREATE INDEX IF NOT EXISTS idx_pbs_player_ts ON personal_bests(player_id, ts);

CREATE TABLE IF NOT EXISTS player_kills (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  opponent_name TEXT NOT NULL,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_player_kills_player_ts ON player_kills(player_id, ts);

CREATE TABLE IF NOT EXISTS player_deaths (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  value_lost INTEGER,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_player_deaths_player_ts ON player_deaths(player_id, ts);

CREATE TABLE IF NOT EXISTS world_changes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  world INTEGER NOT NULL,
  world_types TEXT NOT NULL DEFAULT '[]',
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_world_changes_player_ts ON world_changes(player_id, ts);

CREATE TABLE IF NOT EXISTS net_worth_snapshots (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
  inventory_value INTEGER NOT NULL,
  equipment_value INTEGER NOT NULL,
  bank_value INTEGER NOT NULL,
  total_value INTEGER NOT NULL,
  ts TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_net_worth_player_ts ON net_worth_snapshots(player_id, ts);
