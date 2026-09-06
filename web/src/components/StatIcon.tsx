import type { ReactElement } from "react";

export type StatIconTone =
  | "xp"
  | "combat"
  | "boss"
  | "economy"
  | "progression"
  | "prestige"
  | "collectible"
  | "companion"
  | "world";

const SHAPES: Record<StatIconTone, ReactElement> = {
  xp: <path d="M12 1l2.5 7.5L22 11l-7.5 2.5L12 21l-2.5-7.5L2 11l7.5-2.5L12 1z" />,
  combat: (
    <g>
      <g transform="rotate(45 12 12)">
        <rect x="11" y="2" width="2" height="13" rx="0.6" />
        <rect x="9" y="14" width="6" height="2" rx="0.6" />
        <rect x="11" y="16.5" width="2" height="4" rx="0.6" />
      </g>
      <g transform="rotate(-45 12 12)">
        <rect x="11" y="2" width="2" height="13" rx="0.6" />
        <rect x="9" y="14" width="6" height="2" rx="0.6" />
        <rect x="11" y="16.5" width="2" height="4" rx="0.6" />
      </g>
    </g>
  ),
  boss: (
    <g fill="none" stroke="currentColor" strokeWidth="1.6">
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="5" />
      <circle cx="12" cy="12" r="1.4" fill="currentColor" stroke="none" />
    </g>
  ),
  economy: (
    <g fill="none" stroke="currentColor" strokeWidth="1.6">
      <circle cx="9" cy="15" r="6" />
      <circle cx="15" cy="9" r="6" />
    </g>
  ),
  progression: (
    <g>
      <rect x="4" y="5" width="4" height="4" rx="1" fill="currentColor" />
      <rect x="4" y="10.5" width="4" height="4" rx="1" fill="none" stroke="currentColor" strokeWidth="1.4" />
      <rect x="4" y="16" width="4" height="4" rx="1" fill="none" stroke="currentColor" strokeWidth="1.4" />
      <path d="M11 7h9M11 12.5h9M11 18h9" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
    </g>
  ),
  prestige: (
    <g>
      <path d="M8 2h3l1 4-1 1H9L8 2zM16 2h-3l-1 4 1 1h2l1-5z" fill="currentColor" />
      <circle cx="12" cy="14" r="7" fill="none" stroke="currentColor" strokeWidth="1.8" />
      <path d="M9 12.5l2 2 4-4" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
    </g>
  ),
  collectible: <path d="M12 2l5 5-5 15-5-15 5-5z" />,
  companion: (
    <g>
      <ellipse cx="12" cy="16" rx="5" ry="4" />
      <circle cx="6" cy="9" r="2.2" />
      <circle cx="12" cy="6.5" r="2.4" />
      <circle cx="18" cy="9" r="2.2" />
    </g>
  ),
  world: (
    <g fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="12" cy="12" r="9" />
      <ellipse cx="12" cy="12" rx="4" ry="9" />
      <path d="M3 12h18" />
    </g>
  ),
};

interface StatIconProps {
  tone: StatIconTone;
  className?: string;
}

export function StatIcon({ tone, className }: StatIconProps) {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" className={className} aria-hidden="true">
      {SHAPES[tone]}
    </svg>
  );
}
