/**
 * Item sprites are hotlinked directly from the OSRS Wiki via its
 * `Special:FilePath` redirect (`/w/Special:FilePath/<Item name>.png`, which
 * 30x's through to the current `images/...png` file with a CORS-open
 * `Access-Control-Allow-Origin: *`). Unlike skill icons or the curated boss
 * list, the space of possible dropped items is unbounded — there are tens
 * of thousands of them — so these can't be pre-downloaded and bundled the
 * way `SkillIcon`/`MonsterIcon` are; a lookup miss just falls back to a
 * generic item glyph via `onError`.
 */
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
      src={`https://oldschool.runescape.wiki/w/Special:FilePath/${encodeURIComponent(itemName)}.png`}
      alt=""
      width={20}
      height={20}
      className={className}
      onError={(e) => {
        e.currentTarget.onerror = null;
        e.currentTarget.src = FALLBACK_SVG;
      }}
    />
  );
}
