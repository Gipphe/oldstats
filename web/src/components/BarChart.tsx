import { useEffect, useState, type ReactNode } from "react";
import "./BarChart.css";

export interface BarChartDatum {
  label: string;
  value: number;
  badge?: string;
  color?: string;
  icon?: ReactNode;
}

interface BarChartProps {
  data: BarChartDatum[];
  valueFormatter?: (value: number) => string;
  emptyMessage?: string;
}

const PALETTE = [
  "var(--series-1)",
  "var(--series-2)",
  "var(--series-3)",
  "var(--series-4)",
  "var(--series-5)",
  "var(--series-6)",
  "var(--series-7)",
  "var(--series-8)",
];

export function BarChart({ data, valueFormatter = (v) => v.toLocaleString(), emptyMessage = "No data yet" }: BarChartProps) {
  const [grown, setGrown] = useState(false);

  useEffect(() => {
    // Two rAFs: the first lets the browser paint the 0%-width bars, the
    // second flips the width so the CSS transition actually has something
    // to animate from instead of jumping straight to the final size.
    let raf2 = 0;
    const raf1 = requestAnimationFrame(() => {
      raf2 = requestAnimationFrame(() => setGrown(true));
    });
    return () => {
      cancelAnimationFrame(raf1);
      cancelAnimationFrame(raf2);
    };
  }, []);

  if (data.length === 0) {
    return <p className="barchart-empty">{emptyMessage}</p>;
  }

  const max = Math.max(...data.map((d) => d.value), 1);

  return (
    <div className="barchart" role="img" aria-label={data.map((d) => `${d.label}: ${valueFormatter(d.value)}`).join(", ")}>
      {data.map((d, i) => {
        const color = d.color ?? PALETTE[i % PALETTE.length];
        return (
          <div className="barchart-row" key={d.label}>
            <div className="barchart-row-header">
              {d.icon && (
                <span className="barchart-icon" style={{ color }}>
                  {d.icon}
                </span>
              )}
              <span className="barchart-label">{d.label}</span>
              {d.badge && <span className="barchart-badge">{d.badge}</span>}
            </div>
            <div className="barchart-track">
              <div
                className="barchart-fill"
                style={{
                  width: grown ? `${Math.max((d.value / max) * 100, 3)}%` : "0%",
                  background: color,
                  transitionDelay: `${i * 60}ms`,
                }}
              />
            </div>
            <span className="barchart-value tabular-nums">{valueFormatter(d.value)}</span>
          </div>
        );
      })}
    </div>
  );
}
