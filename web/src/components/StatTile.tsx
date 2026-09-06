import type { ReactNode } from "react";
import { StatIcon, type StatIconTone } from "./StatIcon";
import "./StatTile.css";

interface StatTileProps {
  label: string;
  value: string;
  sublabel?: string;
  tone?: StatIconTone;
  /** Overrides the tone's default icon (e.g. a real item sprite) while keeping its accent color. */
  icon?: ReactNode;
}

export function StatTile({ label, value, sublabel, tone = "xp", icon }: StatTileProps) {
  return (
    <div className="stat-tile" data-tone={tone}>
      <span className="stat-tile-icon">{icon ?? <StatIcon tone={tone} />}</span>
      <div className="stat-tile-body">
        <span className="stat-tile-value tabular-nums">{value}</span>
        <span className="stat-tile-label">{label}</span>
        {sublabel && <span className="stat-tile-sublabel">{sublabel}</span>}
      </div>
    </div>
  );
}
