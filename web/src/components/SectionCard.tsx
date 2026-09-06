import type { CSSProperties, ReactNode } from "react";
import "./SectionCard.css";

interface SectionCardProps {
  title: string;
  action?: ReactNode;
  children: ReactNode;
  /** A CSS color value (e.g. `"var(--series-2)"`) accenting this card's header and top edge. Defaults to gold. */
  accent?: string;
}

export function SectionCard({ title, action, children, accent }: SectionCardProps) {
  const style = accent ? ({ "--card-accent": accent } as CSSProperties) : undefined;
  return (
    <section className="section-card" style={style}>
      <header className="section-card-header">
        <h2>{title}</h2>
        {action}
      </header>
      {children}
    </section>
  );
}
