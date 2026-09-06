/**
 * Item icons are bundled locally under `web/public/items/` — bulk-fetched
 * from the OSRS Wiki price API's item mapping (`prices.runescape.wiki/api/v1/osrs/mapping`,
 * ~4.5k tradeable items, ~18MB of PNGs), which covers the vast majority of
 * anything that shows up as a drop. Anything not in that bundle (untradeable
 * items, or anything added to the game since the bundle was built) falls
 * back to hotlinking the OSRS Wiki directly via its `Special:FilePath`
 * redirect, and if even that 404s, a generic item glyph.
 */
function slugify(name: string): string {
  return name
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

const FALLBACK_SVG =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#8a7a5c" stroke-width="1.6"><path d="M4 8l8-4 8 4v8l-8 4-8-4V8z"/><path d="M4 8l8 4 8-4M12 12v8"/></svg>`,
  );

interface ItemIconProps {
  itemName: string;
  className?: string;
}

export function ItemIcon({ itemName, className }: ItemIconProps) {
  return (
    <img
      src={`/items/${slugify(itemName)}.png`}
      alt=""
      width={20}
      height={20}
      className={className}
      data-stage="local"
      onError={(e) => {
        const img = e.currentTarget;
        if (img.dataset.stage === "local") {
          img.dataset.stage = "wiki";
          img.src = `https://oldschool.runescape.wiki/w/Special:FilePath/${encodeURIComponent(itemName)}.png`;
        } else {
          img.onerror = null;
          img.src = FALLBACK_SVG;
        }
      }}
    />
  );
}
