import { expect, test } from "@playwright/test";
import { statTile } from "./helpers";

test.describe("Bank", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/bank");
    await expect(page.getByRole("heading", { name: "Bank" })).toBeVisible();
  });

  test("shows total value, item count and the full item list", async ({ page }) => {
    await expect(statTile(page, "Total value").locator(".stat-tile-value")).toHaveText("15.23M gp");
    await expect(statTile(page, "Unique items").locator(".stat-tile-value")).toHaveText("5");

    await expect(page.getByText("Draconic visage")).toBeVisible();
    await expect(page.getByText("15,000x Zulrah's scales")).toBeVisible();
    await expect(page.getByText("2,500,000x Coins")).toBeVisible();
  });

  test("search filters the item list down to matches", async ({ page }) => {
    await expect(page.getByRole("heading", { name: /Items \(5\)/ })).toBeVisible();

    await page.getByPlaceholder("Search bank…").fill("zulrah");

    await expect(page.getByRole("heading", { name: /Items \(1\)/ })).toBeVisible();
    await expect(page.getByText("15,000x Zulrah's scales")).toBeVisible();
    await expect(page.getByText("Draconic visage")).toHaveCount(0);
  });
});
