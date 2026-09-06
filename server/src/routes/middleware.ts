import type { NextFunction, Request, Response } from "express";
import type { Database } from "better-sqlite3";
import { getPlayerByApiKey } from "../services/players.js";

export interface AuthedRequest extends Request {
  player?: { id: number; username: string };
}

export function requireApiKey(db: Database) {
  return (req: AuthedRequest, res: Response, next: NextFunction) => {
    const header = req.header("authorization");
    const apiKey = header?.startsWith("Bearer ") ? header.slice("Bearer ".length) : undefined;
    if (!apiKey) {
      res.status(401).json({ error: "Missing Authorization: Bearer <apiKey> header" });
      return;
    }
    const player = getPlayerByApiKey(db, apiKey);
    if (!player) {
      res.status(401).json({ error: "Invalid API key" });
      return;
    }
    req.player = { id: player.id, username: player.username };
    next();
  };
}
