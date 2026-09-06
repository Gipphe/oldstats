import { expect, test } from "@playwright/test";

test.describe("Activity", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/activity");
    await expect(page.getByRole("heading", { name: "Activity" })).toBeVisible();
  });

  test("Kills tab shows kills with the boss badge", async ({ page }) => {
    const vorkath = page.locator(".list-item", { hasText: "Vorkath" });
    await expect(vorkath.getByText("Boss")).toBeVisible();
    await expect(page.locator(".list-item", { hasText: "Abyssal demon" })).toHaveCount(2);
  });

  test("Drops tab shows the visage and its value", async ({ page }) => {
    await page.getByRole("tab", { name: "Drops" }).click();
    const row = page.locator(".list-item", { hasText: "Draconic visage" });
    await expect(row).toContainText("8.50M gp");
  });

  test("Quests tab shows the completed quest", async ({ page }) => {
    await page.getByRole("tab", { name: "Quests" }).click();
    await expect(page.getByText("Dragon Slayer Ii")).toBeVisible();
  });

  test("Slayer tab shows the completed task with its assigned count", async ({ page }) => {
    await page.getByRole("tab", { name: "Slayer" }).click();
    await expect(page.getByText("Abyssal demons (150)")).toBeVisible();
  });

  test("Farming tab shows the harvested patch", async ({ page }) => {
    await page.getByRole("tab", { name: "Farming" }).click();
    const row = page.locator(".list-item", { hasText: "Catherby (South)" });
    await expect(row).toContainText("Ranarr");
    await expect(row).toContainText("Harvested");
  });

  test("Collection log tab shows the unlocked item", async ({ page }) => {
    await page.getByRole("tab", { name: "Collection log" }).click();
    await expect(page.getByText("Draconic visage")).toBeVisible();
  });

  test("Combat achievements tab shows the completed task", async ({ page }) => {
    await page.getByRole("tab", { name: "Combat achievements" }).click();
    await expect(page.getByText("Vorkath Killcount 1")).toBeVisible();
  });

  test("Diaries tab shows the completed diary tier", async ({ page }) => {
    await page.getByRole("tab", { name: "Diaries" }).click();
    await expect(page.getByText("Ardougne — Elite")).toBeVisible();
  });

  test("Clues tab shows the completed tier", async ({ page }) => {
    await page.getByRole("tab", { name: "Clues" }).click();
    await expect(page.locator(".list-item", { hasText: "Elite" })).toBeVisible();
  });

  test("Pets tab shows the received pet", async ({ page }) => {
    await page.getByRole("tab", { name: "Pets" }).click();
    await expect(page.getByText("Vorki")).toBeVisible();
  });

  test("Personal bests tab shows the recorded time", async ({ page }) => {
    await page.getByRole("tab", { name: "Personal bests" }).click();
    const row = page.locator(".list-item", { hasText: "Vorkath" });
    await expect(row).toContainText("1:15.60");
  });

  test("PvP kills tab shows the defeated opponent", async ({ page }) => {
    await page.getByRole("tab", { name: "PvP kills" }).click();
    await expect(page.getByText("Some Pker")).toBeVisible();
  });

  test("Deaths tab shows the value lost", async ({ page }) => {
    await page.getByRole("tab", { name: "Deaths" }).click();
    await expect(page.getByText("500.0K gp", { exact: false })).toBeVisible();
  });
});
