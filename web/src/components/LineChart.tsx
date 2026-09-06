import { useId } from "react";
import "./LineChart.css";

export interface LineChartPoint {
  ts: string;
  value: number;
}

interface LineChartProps {
  data: LineChartPoint[];
  valueFormatter?: (value: number) => string;
  emptyMessage?: string;
}

const WIDTH = 300;
const HEIGHT = 100;
const PAD = 6;

export function LineChart({ data, valueFormatter = (v) => v.toLocaleString(), emptyMessage = "No data yet" }: LineChartProps) {
  const gradientId = useId();

  if (data.length < 2) {
    return <p className="linechart-empty">{emptyMessage}</p>;
  }

  const values = data.map((d) => d.value);
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = max - min || 1;

  const points = data.map((d, i) => {
    const x = PAD + (i / (data.length - 1)) * (WIDTH - PAD * 2);
    const y = HEIGHT - PAD - ((d.value - min) / range) * (HEIGHT - PAD * 2);
    return { x, y };
  });

  const linePath = points.map((p, i) => `${i === 0 ? "M" : "L"}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(" ");
  const areaPath = `${linePath} L${points[points.length - 1].x.toFixed(1)},${HEIGHT - PAD} L${points[0].x.toFixed(1)},${HEIGHT - PAD} Z`;

  const last = data[data.length - 1];
  const first = data[0];
  const change = last.value - first.value;

  return (
    <div className="linechart">
      <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} preserveAspectRatio="none" className="linechart-svg" role="img" aria-label={`${valueFormatter(first.value)} to ${valueFormatter(last.value)}`}>
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" style={{ stopColor: "var(--series-1)", stopOpacity: 0.4 }} />
            <stop offset="100%" style={{ stopColor: "var(--series-1)", stopOpacity: 0 }} />
          </linearGradient>
        </defs>
        <path d={areaPath} className="linechart-area" fill={`url(#${gradientId})`} />
        <path d={linePath} className="linechart-line" />
        <circle cx={points[points.length - 1].x} cy={points[points.length - 1].y} r="3" className="linechart-dot" />
      </svg>
      <div className="linechart-footer">
        <span className="linechart-value tabular-nums">{valueFormatter(last.value)}</span>
        <span className={"linechart-change tabular-nums" + (change < 0 ? " negative" : "")}>
          {change >= 0 ? "+" : ""}
          {valueFormatter(change)}
        </span>
      </div>
    </div>
  );
}
