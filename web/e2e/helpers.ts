import type { Page } from "@playwright/test";

function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

/**
 * Locates a `.stat-tile` by its exact label text. Plain `hasText` substring
 * matching is too loose here — e.g. "Kills" also substring-matches "Boss
 * kills" and a PvP tile's "N kills, N deaths" sublabel.
 */
export function statTile(page: Page, label: string) {
  return page.locator(".stat-tile").filter({
    has: page.locator(".stat-tile-label", { hasText: new RegExp(`^${escapeRegExp(label)}$`) }),
  });
}
