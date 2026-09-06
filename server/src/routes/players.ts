import { Router } from "express";
import type { Database } from "better-sqlite3";
import { z } from "zod";
import { createPlayer, getPlayerByUsername, listPlayers } from "../services/players.js";

const createPlayerBody = z.object({
  username: z.string().min(1).max(64),
  accountHash: z.string().optional(),
});

export function playersRouter(db: Database): Router {
  const router = Router();

  router.get("/players", (_req, res) => {
    const players = listPlayers(db).map((p) => ({ id: p.id, username: p.username, createdAt: p.createdAt }));
    res.json(players);
  });

  router.post("/players", (req, res) => {
    const registrationSecret = process.env.OLDSTATS_REGISTRATION_SECRET;
    if (registrationSecret) {
      const provided = req.header("x-registration-secret");
      if (provided !== registrationSecret) {
        res.status(403).json({ error: "Invalid or missing registration secret" });
        return;
      }
    }

    const parsed = createPlayerBody.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: "Invalid payload", details: parsed.error.flatten() });
      return;
    }

    if (getPlayerByUsername(db, parsed.data.username)) {
      res.status(409).json({ error: "Username already registered" });
      return;
    }

    const player = createPlayer(db, parsed.data.username, parsed.data.accountHash);
    // apiKey is only ever returned once, at creation time — the plugin config panel should store it.
    res.status(201).json({ id: player.id, username: player.username, apiKey: player.apiKey });
  });

  return router;
}
