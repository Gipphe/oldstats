import type { ReactNode } from "react";
import "./SectionCard.css";

interface SectionCardProps {
  title: string;
  action?: ReactNode;
  children: ReactNode;
}

export function SectionCard({ title, action, children }: SectionCardProps) {
  return (
    <section className="section-card">
      <header className="section-card-header">
        <h2>{title}</h2>
        {action}
      </header>
      {children}
    </section>
  );
}
