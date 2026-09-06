import express, { type Express } from "express";
import cors from "cors";
import type { Database } from "better-sqlite3";
import { ingestRouter } from "./routes/ingest.js";
import { playersRouter } from "./routes/players.js";
import { statsRouter } from "./routes/stats.js";

export function createApp(db: Database): Express {
  const app = express();
  app.use(cors());
  app.use(express.json({ limit: "1mb" }));

  app.get("/health", (_req, res) => {
    res.json({ status: "ok" });
  });

  app.use("/api", playersRouter(db));
  app.use("/api", statsRouter(db));
  app.use("/api", ingestRouter(db));

  app.use((err: unknown, _req: express.Request, res: express.Response, _next: express.NextFunction) => {
    console.error(err);
    res.status(500).json({ error: "Internal server error" });
  });

  return app;
}
