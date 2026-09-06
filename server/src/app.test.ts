import { afterEach, beforeEach, describe, expect, it } from "vitest";
import request from "supertest";
import type { Express } from "express";
import { freshDb } from "./test-helpers.js";
import { createApp } from "./app.js";

describe("app (integration)", () => {
  let app: Express;

  beforeEach(() => {
    app = createApp(freshDb());
  });

  afterEach(() => {
    delete process.env.OLDSTATS_REGISTRATION_SECRET;
  });

  it("GET /health returns ok", async () => {
    const res = await request(app).get("/health");
    expect(res.status).toBe(200);
    expect(res.body).toEqual({ status: "ok" });
  });

  describe("POST /api/players", () => {
    it("registers a new player and returns an api key", async () => {
      const res = await request(app).post("/api/players").send({ username: "zezima" });
      expect(res.status).toBe(201);
      expect(res.body.username).toBe("zezima");
      expect(typeof res.body.apiKey).toBe("string");
      expect(res.body.apiKey.length).toBeGreaterThan(10);
    });

    it("rejects a duplicate username with 409", async () => {
      await request(app).post("/api/players").send({ username: "zezima" });
      const res = await request(app).post("/api/players").send({ username: "zezima" });
      expect(res.status).toBe(409);
    });

    it("rejects an invalid payload with 400", async () => {
      const res = await request(app).post("/api/players").send({ username: "" });
      expect(res.status).toBe(400);
    });

    it("requires the registration secret header when configured", async () => {
      process.env.OLDSTATS_REGISTRATION_SECRET = "s3cr3t";

      const withoutSecret = await request(app).post("/api/players").send({ username: "zezima" });
      expect(withoutSecret.status).toBe(403);

      const withWrongSecret = await request(app)
        .post("/api/players")
        .set("x-registration-secret", "wrong")
        .send({ username: "zezima" });
      expect(withWrongSecret.status).toBe(403);

      const withCorrectSecret = await request(app)
        .post("/api/players")
        .set("x-registration-secret", "s3cr3t")
        .send({ username: "zezima" });
      expect(withCorrectSecret.status).toBe(201);
    });
  });

  describe("POST /api/events", () => {
    it("rejects a request without an Authorization header", async () => {
      const res = await request(app)
        .post("/api/events")
        .send({ events: [{ type: "pet_received", ts: "2026-01-01T00:00:00.000Z" }] });
      expect(res.status).toBe(401);
    });

    it("rejects an invalid api key", async () => {
      const res = await request(app)
        .post("/api/events")
        .set("Authorization", "Bearer not-a-real-key")
        .send({ events: [{ type: "pet_received", ts: "2026-01-01T00:00:00.000Z" }] });
      expect(res.status).toBe(401);
    });

    it("rejects an invalid event payload with 400", async () => {
      const register = await request(app).post("/api/players").send({ username: "zezima" });
      const res = await request(app)
        .post("/api/events")
        .set("Authorization", `Bearer ${register.body.apiKey}`)
        .send({ events: [{ type: "xp_gain" }] });
      expect(res.status).toBe(400);
    });

    it("accepts a valid batch and makes it readable via the stats endpoints", async () => {
      const register = await request(app).post("/api/players").send({ username: "zezima" });
      const apiKey = register.body.apiKey as string;

      const ingest = await request(app)
        .post("/api/events")
        .set("Authorization", `Bearer ${apiKey}`)
        .send({
          events: [
            {
              type: "xp_gain",
              skill: "SLAYER",
              xp: 100,
              xpGained: 100,
              level: 10,
              ts: "2026-01-01T00:00:00.000Z",
            },
          ],
        });
      expect(ingest.status).toBe(202);
      expect(ingest.body.accepted).toBe(1);

      const overviewByUsername = await request(app).get("/api/players/zezima/overview");
      expect(overviewByUsername.status).toBe(200);
      expect(overviewByUsername.body.totalXpGained).toBe(100);

      const overviewById = await request(app).get(`/api/players/${register.body.id}/overview`);
      expect(overviewById.status).toBe(200);
      expect(overviewById.body.totalXpGained).toBe(100);
    });
  });

  describe("player resolution", () => {
    it("returns 404 for an unknown player", async () => {
      const res = await request(app).get("/api/players/nobody/overview");
      expect(res.status).toBe(404);
    });
  });
});
