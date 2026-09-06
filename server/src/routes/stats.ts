import { Router } from "express";
import type { Database } from "better-sqlite3";
import {
  getOverview,
  getXpSeries,
  getXpBySkill,
  getLevelUps,
  getKills,
  getKillsByNpc,
  getDrops,
  getTopDrops,
  getQuests,
  getSlayerTasks,
  getFarmingPatches,
  getFarmingHistory,
  getCollectionLog,
  getCombatAchievements,
  getDiaries,
  getClues,
  getPets,
  getPersonalBests,
  getPlayerKills,
  getPlayerDeaths,
  getWorldBreakdown,
  getNetWorthHistory,
  getBank,
  type DateRange,
} from "../services/stats.js";
import { getWeeklySummary } from "../services/weeklySummary.js";
import { getPlayerByUsername } from "../services/players.js";

function parseRange(req: import("express").Request): DateRange {
  const since = typeof req.query.since === "string" ? req.query.since : undefined;
  const until = typeof req.query.until === "string" ? req.query.until : undefined;
  return { since, until };
}

function resolvePlayerId(db: Database, idParam: string): number | undefined {
  if (/^\d+$/.test(idParam)) return Number(idParam);
  return getPlayerByUsername(db, idParam)?.id;
}

export function statsRouter(db: Database): Router {
  const router = Router();

  router.use("/players/:playerId", (req, res, next) => {
    const id = resolvePlayerId(db, req.params.playerId);
    if (id === undefined) {
      res.status(404).json({ error: "Player not found" });
      return;
    }
    (req as any).resolvedPlayerId = id;
    next();
  });

  router.get("/players/:playerId/overview", (req, res) => {
    res.json(getOverview(db, (req as any).resolvedPlayerId));
  });

  router.get("/players/:playerId/xp", (req, res) => {
    const range = parseRange(req);
    const skill = typeof req.query.skill === "string" ? req.query.skill : undefined;
    res.json(getXpSeries(db, (req as any).resolvedPlayerId, range, skill));
  });

  router.get("/players/:playerId/xp/by-skill", (req, res) => {
    const range = parseRange(req);
    res.json(getXpBySkill(db, (req as any).resolvedPlayerId, range));
  });

  router.get("/players/:playerId/level-ups", (req, res) => {
    const range = parseRange(req);
    res.json(getLevelUps(db, (req as any).resolvedPlayerId, range));
  });

  router.get("/players/:playerId/kills", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getKills(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/kills/by-npc", (req, res) => {
    const range = parseRange(req);
    res.json(getKillsByNpc(db, (req as any).resolvedPlayerId, range));
  });

  router.get("/players/:playerId/drops", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getDrops(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/drops/top", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getTopDrops(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/quests", (req, res) => {
    res.json(getQuests(db, (req as any).resolvedPlayerId));
  });

  router.get("/players/:playerId/slayer", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getSlayerTasks(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/farming", (req, res) => {
    res.json(getFarmingPatches(db, (req as any).resolvedPlayerId));
  });

  router.get("/players/:playerId/farming/history", (req, res) => {
    const range = parseRange(req);
    res.json(getFarmingHistory(db, (req as any).resolvedPlayerId, range));
  });

  router.get("/players/:playerId/collection-log", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getCollectionLog(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/combat-achievements", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getCombatAchievements(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/diaries", (req, res) => {
    res.json(getDiaries(db, (req as any).resolvedPlayerId));
  });

  router.get("/players/:playerId/clues", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getClues(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/pets", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getPets(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/personal-bests", (req, res) => {
    res.json(getPersonalBests(db, (req as any).resolvedPlayerId));
  });

  router.get("/players/:playerId/pvp/kills", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getPlayerKills(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/pvp/deaths", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getPlayerDeaths(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/worlds", (req, res) => {
    const range = parseRange(req);
    res.json(getWorldBreakdown(db, (req as any).resolvedPlayerId, range));
  });

  router.get("/players/:playerId/net-worth", (req, res) => {
    const range = parseRange(req);
    const limit = req.query.limit ? Number(req.query.limit) : undefined;
    res.json(getNetWorthHistory(db, (req as any).resolvedPlayerId, range, limit));
  });

  router.get("/players/:playerId/bank", (req, res) => {
    res.json(getBank(db, (req as any).resolvedPlayerId));
  });

  router.get("/players/:playerId/weekly-summary", (req, res) => {
    const now = new Date();
    const defaultEnd = now.toISOString();
    const defaultStart = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString();
    const weekStart = typeof req.query.weekStart === "string" ? req.query.weekStart : defaultStart;
    const weekEnd = typeof req.query.weekEnd === "string" ? req.query.weekEnd : defaultEnd;
    res.json(getWeeklySummary(db, (req as any).resolvedPlayerId, weekStart, weekEnd));
  });

  return router;
}
