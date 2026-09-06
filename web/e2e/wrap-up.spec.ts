import { expect, test, type Page } from "@playwright/test";

function goToSlide(page: Page, title: string) {
  return page.getByRole("button", { name: `Go to ${title}` }).click();
}

test.describe("Weekly wrap-up", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/wrap-up");
    await expect(page.getByRole("heading", { name: "Weekly wrap-up" })).toBeVisible();
    // Wait for the summary to load past the initial "Loading…" state.
    await expect(page.getByText("Loading…")).toHaveCount(0);
  });

  test("opens on the highlights slide, one of seven", async ({ page }) => {
    await expect(page.getByRole("heading", { name: "Your Week" })).toBeVisible();
    await expect(page.locator(".wrapup-slide-counter")).toHaveText("1 / 7");
    await expect(page.locator(".wrapup-dot")).toHaveCount(7);
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

  test("skilling slide shows total XP and the top skills chart", async ({ page }) => {
    await goToSlide(page, "Skilling");
    await expect(page.locator(".wrapup-slide-counter")).toHaveText("2 / 7");
    await expect(page.locator(".wrapup-hero-value")).toHaveText("13,000");
    await expect(page.locator(".barchart-row", { hasText: "Magic" }).locator(".barchart-value")).toHaveText("8,000");
  });

  test("combat slide shows kills and the most-killed chart", async ({ page }) => {
    await goToSlide(page, "Combat");
    await expect(page.locator(".wrapup-hero-value")).toHaveText("3");
    await expect(page.locator(".barchart-row", { hasText: "Vorkath" })).toContainText("Boss");
  });

  test("loot slide shows the best drop and net worth change", async ({ page }) => {
    await goToSlide(page, "Loot & Wealth");
    const slide = page.locator(".wrapup-slide");
    await expect(slide).toContainText("Draconic visage");
    await expect(slide).toContainText("8.50M gp");
  });

  test("slayer and PvP slide shows the kill-death record", async ({ page }) => {
    await goToSlide(page, "Slayer & PvP");
    await expect(page.locator(".wrapup-hero-value", { hasText: "1-1" })).toBeVisible();
  });

  test("back and next buttons step through slides, disabled at the ends", async ({ page }) => {
    const back = page.getByRole("button", { name: "‹ Back" });
    const next = page.getByRole("button", { name: "Next ›" });
    await expect(back).toBeDisabled();

    await next.click();
    await expect(page.locator(".wrapup-slide-counter")).toHaveText("2 / 7");
    await expect(back).toBeEnabled();

    await back.click();
    await expect(page.locator(".wrapup-slide-counter")).toHaveText("1 / 7");
    await expect(back).toBeDisabled();

    for (let i = 0; i < 6; i++) await next.click();
    await expect(page.locator(".wrapup-slide-counter")).toHaveText("7 / 7");
    await expect(next).toBeDisabled();
  });

  test("switching weeks resets back to the first slide", async ({ page }) => {
    await goToSlide(page, "Combat");
    await expect(page.locator(".wrapup-slide-counter")).toHaveText("3 / 7");

    await page.getByRole("button", { name: "Previous week" }).click();
    await expect(page.getByText("Loading…")).toHaveCount(0);
    await expect(page.locator(".wrapup-slide-counter")).toHaveText("1 / 7");
  });
});
