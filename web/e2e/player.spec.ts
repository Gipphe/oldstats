import { expect, test } from "@playwright/test";
import { PLAYER_USERNAME } from "./seed-data";

test("shows the seeded player, auto-selected since it's the only one", async ({ page }) => {
  await page.goto("/player");

  const playerButton = page.locator(".player-list-item", { hasText: PLAYER_USERNAME });
  await expect(playerButton).toBeVisible();
  await expect(playerButton.getByText("Selected")).toBeVisible();
});
