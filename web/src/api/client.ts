import type {
  Bank,
  Clue,
  CollectionLogItem,
  CombatAchievement,
  Diary,
  Drop,
  FarmingPatch,
  Kill,
  KillByNpc,
  LevelUp,
  NetWorthSnapshot,
  Overview,
  PersonalBest,
  Pet,
  Player,
  PlayerDeath,
  PlayerKill,
  Quest,
  SlayerTask,
  WeeklySummary,
  WorldBreakdown,
  XpBySkill,
} from "./types";

const API_BASE_URL: string = import.meta.env.VITE_API_URL ?? "http://localhost:4000";

async function get<T>(path: string, params?: Record<string, string | number | undefined>): Promise<T> {
  const url = new URL(`${API_BASE_URL}/api${path}`);
  if (params) {
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined) url.searchParams.set(key, String(value));
    }
  }
  const res = await fetch(url.toString());
  if (!res.ok) {
    throw new Error(`Request to ${path} failed: ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export interface DateRange {
  since?: string;
  until?: string;
  [key: string]: string | number | undefined;
}

export const api = {
  listPlayers: () => get<Player[]>("/players"),

  overview: (playerId: number | string) => get<Overview>(`/players/${playerId}/overview`),

  xpBySkill: (playerId: number | string, range: DateRange = {}) =>
    get<XpBySkill[]>(`/players/${playerId}/xp/by-skill`, range),

  levelUps: (playerId: number | string, range: DateRange = {}) =>
    get<LevelUp[]>(`/players/${playerId}/level-ups`, range),

  kills: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<Kill[]>(`/players/${playerId}/kills`, { ...range, limit }),

  killsByNpc: (playerId: number | string, range: DateRange = {}) =>
    get<KillByNpc[]>(`/players/${playerId}/kills/by-npc`, range),

  drops: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<Drop[]>(`/players/${playerId}/drops`, { ...range, limit }),

  topDrops: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<Drop[]>(`/players/${playerId}/drops/top`, { ...range, limit }),

  quests: (playerId: number | string) => get<Quest[]>(`/players/${playerId}/quests`),

  slayerTasks: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<SlayerTask[]>(`/players/${playerId}/slayer`, { ...range, limit }),

  farmingPatches: (playerId: number | string) => get<FarmingPatch[]>(`/players/${playerId}/farming`),

  farmingHistory: (playerId: number | string, range: DateRange = {}) =>
    get<FarmingPatch[]>(`/players/${playerId}/farming/history`, range),

  weeklySummary: (playerId: number | string, weekStart: string, weekEnd: string) =>
    get<WeeklySummary>(`/players/${playerId}/weekly-summary`, { weekStart, weekEnd }),

  collectionLog: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<CollectionLogItem[]>(`/players/${playerId}/collection-log`, { ...range, limit }),

  combatAchievements: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<CombatAchievement[]>(`/players/${playerId}/combat-achievements`, { ...range, limit }),

  diaries: (playerId: number | string) => get<Diary[]>(`/players/${playerId}/diaries`),

  clues: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<Clue[]>(`/players/${playerId}/clues`, { ...range, limit }),

  pets: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<Pet[]>(`/players/${playerId}/pets`, { ...range, limit }),

  personalBests: (playerId: number | string) => get<PersonalBest[]>(`/players/${playerId}/personal-bests`),

  playerKills: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<PlayerKill[]>(`/players/${playerId}/pvp/kills`, { ...range, limit }),

  playerDeaths: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<PlayerDeath[]>(`/players/${playerId}/pvp/deaths`, { ...range, limit }),

  worlds: (playerId: number | string, range: DateRange = {}) =>
    get<WorldBreakdown[]>(`/players/${playerId}/worlds`, range),

  netWorth: (playerId: number | string, range: DateRange = {}, limit?: number) =>
    get<NetWorthSnapshot[]>(`/players/${playerId}/net-worth`, { ...range, limit }),

  bank: (playerId: number | string) => get<Bank>(`/players/${playerId}/bank`),
};
