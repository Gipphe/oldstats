import "./BarChart.css";

export interface BarChartDatum {
  label: string;
  value: number;
  badge?: string;
  color?: string;
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
  if (data.length === 0) {
    return <p className="barchart-empty">{emptyMessage}</p>;
  }

  const max = Math.max(...data.map((d) => d.value), 1);

  return (
    <div className="barchart" role="img" aria-label={data.map((d) => `${d.label}: ${valueFormatter(d.value)}`).join(", ")}>
      {data.map((d, i) => (
        <div className="barchart-row" key={d.label}>
          <div className="barchart-row-header">
            <span className="barchart-label">{d.label}</span>
            {d.badge && <span className="barchart-badge">{d.badge}</span>}
          </div>
          <div className="barchart-track">
            <div
              className="barchart-fill"
              style={{
                width: `${Math.max((d.value / max) * 100, 3)}%`,
                background: d.color ?? PALETTE[i % PALETTE.length],
              }}
            />
          </div>
          <span className="barchart-value tabular-nums">{valueFormatter(d.value)}</span>
        </div>
      ))}
    </div>
  );
}
