import { expect, test } from "@playwright/test";
import { statTile } from "./helpers";
import { PLAYER_USERNAME } from "./seed-data";

test.describe("Dashboard", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await expect(page.getByRole("heading", { name: PLAYER_USERNAME })).toBeVisible();
  });

  test("stat tiles reflect seeded data", async ({ page }) => {
    await expect(statTile(page, "Total XP").locator(".stat-tile-value")).toHaveText("13,000");
    await expect(statTile(page, "Kills").locator(".stat-tile-value")).toHaveText("3");
    await expect(statTile(page, "Boss kills").locator(".stat-tile-value")).toHaveText("1");
    await expect(statTile(page, "Loot value").locator(".stat-tile-value")).toHaveText("8.50M gp");
    await expect(statTile(page, "Quests done").locator(".stat-tile-value")).toHaveText("1");
    await expect(statTile(page, "Slayer tasks").locator(".stat-tile-value")).toHaveText("1");
    await expect(statTile(page, "Collection log").locator(".stat-tile-value")).toHaveText("46");
    await expect(statTile(page, "Collection log").locator(".stat-tile-sublabel")).toHaveText("of 1600");
    await expect(statTile(page, "Combat achievements").locator(".stat-tile-value")).toHaveText("1");
    await expect(statTile(page, "Diaries completed").locator(".stat-tile-value")).toHaveText("1");
    await expect(statTile(page, "Clues completed").locator(".stat-tile-value")).toHaveText("1");
    await expect(statTile(page, "Pets received").locator(".stat-tile-value")).toHaveText("1");
    await expect(statTile(page, "PvP K/D").locator(".stat-tile-value")).toHaveText("1.00");
    await expect(statTile(page, "Net worth").locator(".stat-tile-value")).toHaveText("10.15M gp");
  });

  test("XP by skill chart shows both trained skills", async ({ page }) => {
    const section = page.locator(".section-card", { hasText: "XP by skill" });
    const magicRow = section.locator(".barchart-row", { hasText: "Magic" });
    const slayerRow = section.locator(".barchart-row", { hasText: "Slayer" });
    await expect(magicRow.locator(".barchart-value")).toHaveText("8,000");
    await expect(slayerRow.locator(".barchart-value")).toHaveText("5,000");
  });

  test("most-killed chart flags Vorkath as a boss", async ({ page }) => {
    const section = page.locator(".section-card", { hasText: "Most killed" });
    const vorkathRow = section.locator(".barchart-row", { hasText: "Vorkath" });
    await expect(vorkathRow.locator(".barchart-badge")).toHaveText("Boss");
    await expect(vorkathRow.locator(".barchart-value")).toHaveText("1");

    const abyssalRow = section.locator(".barchart-row", { hasText: "Abyssal demon" });
    await expect(abyssalRow.locator(".barchart-value")).toHaveText("2");
    await expect(abyssalRow.locator(".barchart-badge")).toHaveCount(0);
  });

  test("net worth over time chart renders a non-empty trend", async ({ page }) => {
    const section = page.locator(".section-card", { hasText: "Net worth over time" });
    await expect(section.locator(".linechart-empty")).toHaveCount(0);
    await expect(section.locator(".linechart-value")).toHaveText("10.15M gp");
  });

  test("worlds played chart shows both worlds with the PvP badge on the right one", async ({ page }) => {
    const section = page.locator(".section-card", { hasText: "Worlds played" });
    await expect(section.locator(".barchart-row", { hasText: "World 420" })).toBeVisible();
    const pvpWorldRow = section.locator(".barchart-row", { hasText: "World 421" });
    await expect(pvpWorldRow.locator(".barchart-badge")).toHaveText("PvP");
  });
});
