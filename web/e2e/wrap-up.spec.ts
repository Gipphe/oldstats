import { expect, test } from "@playwright/test";
import { statTile } from "./helpers";

test.describe("Weekly wrap-up", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/wrap-up");
    await expect(page.getByRole("heading", { name: "Weekly wrap-up" })).toBeVisible();
    // Wait for the summary to load past the initial "Loading…" state.
    await expect(page.getByText("Loading…")).toHaveCount(0);
  });

  test("highlights mention this week's key events", async ({ page }) => {
    const highlights = page.locator(".highlight-list");
    await expect(highlights).toContainText("Gained 13,000 total XP this week.");
    await expect(highlights).toContainText("Most trained skill: Magic (+8,000 xp).");
    await expect(highlights).toContainText("Best drop: Draconic visage worth 8.50M gp from Vorkath.");
    await expect(highlights).toContainText("Killed 1 boss.");
    await expect(highlights).toContainText("Completed 1 quest.");
    await expect(highlights).toContainText("Unlocked 1 collection log item: Draconic visage.");
    await expect(highlights).toContainText("Completed 1 combat achievement");
    await expect(highlights).toContainText("Completed 1 clue scroll.");
    await expect(highlights).toContainText("Received 1 pet: Vorki.");
    await expect(highlights).toContainText("Set 1 new personal best.");
    await expect(highlights).toContainText("Defeated 1 player in combat.");
    await expect(highlights).toContainText("Died 1 time.");
  });

  test("top skills chart and best drops list match the highlights", async ({ page }) => {
    const topSkills = page.locator(".section-card", { hasText: "Top skills" });
    await expect(topSkills.locator(".barchart-row", { hasText: "Magic" }).locator(".barchart-value")).toHaveText(
      "8,000",
    );

    const bestDrops = page.locator(".section-card", { hasText: "Best drops" });
    await expect(bestDrops).toContainText("Draconic visage");
    await expect(bestDrops).toContainText("8.50M gp");
  });

  test("PvP and net worth stat tiles are present", async ({ page }) => {
    await expect(statTile(page, "PvP").locator(".stat-tile-value")).toHaveText("1-1");
  });
});
