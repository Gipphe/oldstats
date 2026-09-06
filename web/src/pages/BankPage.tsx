import { useMemo, useState } from "react";
import { api } from "../api/client";
import { SectionCard } from "../components/SectionCard";
import { StatTile } from "../components/StatTile";
import { useApiData } from "../hooks/useApiData";
import { formatDateTime, formatGp, formatNumber } from "../lib/format";
import { usePlayer } from "../state/PlayerContext";
import "../styles/shared.css";

export function BankPage() {
  const { selectedPlayer } = usePlayer();
  const [query, setQuery] = useState("");

  const bank = useApiData(
    () => (selectedPlayer ? api.bank(selectedPlayer.id) : Promise.reject(new Error("no player"))),
    [selectedPlayer?.id],
  );

  const filteredItems = useMemo(() => {
    const items = bank.data?.items ?? [];
    if (!query.trim()) return items;
    const needle = query.trim().toLowerCase();
    return items.filter((item) => item.itemName.toLowerCase().includes(needle));
  }, [bank.data, query]);

  if (!selectedPlayer) {
    return (
      <div className="page">
        <h1>Bank</h1>
        <p className="muted">Select a player on the Player tab to see their bank.</p>
      </div>
    );
  }

  const data = bank.data;

  return (
    <div className="page">
      <h1>Bank</h1>

      {bank.loading && <p className="muted">Loading…</p>}
      {bank.error && <p className="error">{bank.error}</p>}

      {data && data.items.length === 0 && (
        <p className="muted">
          No bank snapshot yet. Open your bank in-game with the OldStats plugin running — the plugin can only
          read bank contents once you've opened it at least once that session.
        </p>
      )}

      {data && data.items.length > 0 && (
        <>
          <div className="stat-grid">
            <StatTile tone="economy" label="Total value" value={`${formatGp(data.totalValue)} gp`} />
            <StatTile tone="collectible" label="Unique items" value={formatNumber(data.itemCount)} />
          </div>
          {data.lastSyncedAt && <p className="muted">Last synced {formatDateTime(data.lastSyncedAt)}</p>}

          <input
            className="search-input"
            type="search"
            placeholder="Search bank…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />

          <SectionCard title={`Items (${filteredItems.length})`} accent="var(--series-3)">
            {filteredItems.length === 0 ? (
              <p className="muted">No items match "{query}"</p>
            ) : (
              <ul className="list">
                {filteredItems.map((item, i) => (
                  <li key={i} className="list-item">
                    <span>
                      {item.quantity > 1 ? `${formatNumber(item.quantity)}x ` : ""}
                      {item.itemName}
                    </span>
                    <span className="list-item-meta tabular-nums">{formatGp(item.value)} gp</span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>
        </>
      )}
    </div>
  );
}
