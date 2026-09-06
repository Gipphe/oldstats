import { StatIcon, type StatIconTone } from "./StatIcon";
import "./StatTile.css";

interface StatTileProps {
  label: string;
  value: string;
  sublabel?: string;
  tone?: StatIconTone;
}

export function StatTile({ label, value, sublabel, tone = "xp" }: StatTileProps) {
  return (
    <div className="stat-tile" data-tone={tone}>
      <span className="stat-tile-icon">
        <StatIcon tone={tone} />
      </span>
      <div className="stat-tile-body">
        <span className="stat-tile-value tabular-nums">{value}</span>
        <span className="stat-tile-label">{label}</span>
        {sublabel && <span className="stat-tile-sublabel">{sublabel}</span>}
      </div>
    </div>
  );
}
