import express from "express";
import cors from "cors";
import { db } from "./db/index.js";
import { ingestRouter } from "./routes/ingest.js";
import { playersRouter } from "./routes/players.js";
import { statsRouter } from "./routes/stats.js";

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

const port = Number(process.env.PORT ?? 4000);
app.listen(port, () => {
  console.log(`oldstats-server listening on http://localhost:${port}`);
});
