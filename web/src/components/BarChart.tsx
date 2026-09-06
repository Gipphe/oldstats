import "./BarChart.css";

export interface BarChartDatum {
  label: string;
  value: number;
  badge?: string;
}

interface BarChartProps {
  data: BarChartDatum[];
  valueFormatter?: (value: number) => string;
  emptyMessage?: string;
}

export function BarChart({ data, valueFormatter = (v) => v.toLocaleString(), emptyMessage = "No data yet" }: BarChartProps) {
  if (data.length === 0) {
    return <p className="barchart-empty">{emptyMessage}</p>;
  }

  const max = Math.max(...data.map((d) => d.value), 1);

  return (
    <div className="barchart" role="img" aria-label={data.map((d) => `${d.label}: ${valueFormatter(d.value)}`).join(", ")}>
      {data.map((d) => (
        <div className="barchart-row" key={d.label}>
          <div className="barchart-row-header">
            <span className="barchart-label">{d.label}</span>
            {d.badge && <span className="barchart-badge">{d.badge}</span>}
          </div>
          <div className="barchart-track">
            <div className="barchart-fill" style={{ width: `${Math.max((d.value / max) * 100, 3)}%` }} />
          </div>
          <span className="barchart-value tabular-nums">{valueFormatter(d.value)}</span>
        </div>
      ))}
    </div>
  );
}
