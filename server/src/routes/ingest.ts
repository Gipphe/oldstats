import { Router } from "express";
import type { Database } from "better-sqlite3";
import { ingestBatch } from "../types/events.js";
import { ingestEvents } from "../services/ingest.js";
import { requireApiKey, type AuthedRequest } from "./middleware.js";

export function ingestRouter(db: Database): Router {
  const router = Router();

  router.post("/events", requireApiKey(db), (req: AuthedRequest, res) => {
    const parsed = ingestBatch.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: "Invalid payload", details: parsed.error.flatten() });
      return;
    }
    ingestEvents(db, req.player!.id, parsed.data.events);
    res.status(202).json({ accepted: parsed.data.events.length });
  });

  return router;
}
