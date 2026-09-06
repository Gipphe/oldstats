/**
 * Boss icon sprites downloaded from the OSRS Wiki (RuneLite's client jar
 * only bundles skill icons, not monster art — see `web/public/monsters/`).
 * Only covers the curated boss set the plugin already tracks
 * (`plugin/src/main/kotlin/com/oldstats/tracking/BossList.kt`); anything
 * else (regular monsters, or a boss whose wiki lookup didn't resolve) falls
 * back to a generic skull glyph via `onError`.
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
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#8a7a5c"><path d="M12 2a7 7 0 0 0-7 7c0 3 1.5 5 3 6.5V19a1 1 0 0 0 1 1h1v2h1v-2h2v2h1v-2h1a1 1 0 0 0 1-1v-3.5c1.5-1.5 3-3.5 3-6.5a7 7 0 0 0-7-7z"/><circle cx="9.3" cy="10" r="1.6" fill="#17120b"/><circle cx="14.7" cy="10" r="1.6" fill="#17120b"/></svg>`,
  );

interface MonsterIconProps {
  npcName: string;
  className?: string;
}

export function MonsterIcon({ npcName, className }: MonsterIconProps) {
  const slug = slugify(npcName);
  return (
    <img
      src={`/monsters/${slug}.png`}
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
