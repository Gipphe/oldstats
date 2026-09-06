import { useState } from "react";
import { api } from "../api/client";
import { MonsterIcon } from "../components/MonsterIcon";
import { SectionCard } from "../components/SectionCard";
import { useApiData } from "../hooks/useApiData";
import { formatDateTime, formatDuration, formatGp, titleCase } from "../lib/format";
import { usePlayer } from "../state/PlayerContext";
import "../styles/shared.css";

type Tab =
  | "kills"
  | "drops"
  | "quests"
  | "slayer"
  | "farming"
  | "collection"
  | "combat"
  | "diaries"
  | "clues"
  | "pets"
  | "pbs"
  | "pvpKills"
  | "pvpDeaths";

const TABS: { id: Tab; label: string }[] = [
  { id: "kills", label: "Kills" },
  { id: "drops", label: "Drops" },
  { id: "quests", label: "Quests" },
  { id: "slayer", label: "Slayer" },
  { id: "farming", label: "Farming" },
  { id: "collection", label: "Collection log" },
  { id: "combat", label: "Combat achievements" },
  { id: "diaries", label: "Diaries" },
  { id: "clues", label: "Clues" },
  { id: "pets", label: "Pets" },
  { id: "pbs", label: "Personal bests" },
  { id: "pvpKills", label: "PvP kills" },
  { id: "pvpDeaths", label: "Deaths" },
];

export function ActivityPage() {
  const { selectedPlayer } = usePlayer();
  const [tab, setTab] = useState<Tab>("kills");

  if (!selectedPlayer) {
    return (
      <div className="page">
        <h1>Activity</h1>
        <p className="muted">Select a player on the Player tab to see activity.</p>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Activity</h1>
      <div className="tabs" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.id}
            role="tab"
            aria-selected={tab === t.id}
            className={"tab-button" + (tab === t.id ? " active" : "")}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === "kills" && <KillsTab playerId={selectedPlayer.id} />}
      {tab === "drops" && <DropsTab playerId={selectedPlayer.id} />}
      {tab === "quests" && <QuestsTab playerId={selectedPlayer.id} />}
      {tab === "slayer" && <SlayerTab playerId={selectedPlayer.id} />}
      {tab === "farming" && <FarmingTab playerId={selectedPlayer.id} />}
      {tab === "collection" && <CollectionLogTab playerId={selectedPlayer.id} />}
      {tab === "combat" && <CombatAchievementsTab playerId={selectedPlayer.id} />}
      {tab === "diaries" && <DiariesTab playerId={selectedPlayer.id} />}
      {tab === "clues" && <CluesTab playerId={selectedPlayer.id} />}
      {tab === "pets" && <PetsTab playerId={selectedPlayer.id} />}
      {tab === "pbs" && <PersonalBestsTab playerId={selectedPlayer.id} />}
      {tab === "pvpKills" && <PvpKillsTab playerId={selectedPlayer.id} />}
      {tab === "pvpDeaths" && <PvpDeathsTab playerId={selectedPlayer.id} />}
    </div>
  );
}

function KillsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.kills(playerId, {}, 100), [playerId]);
  return (
    <SectionCard title="Recent kills" accent="var(--series-2)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((k, i) => (
          <li key={i} className="list-item">
            <span className="list-item-name">
              <MonsterIcon npcName={k.npcName} />
              {k.npcName} {k.isBoss ? <span className="inline-badge">Boss</span> : null}
            </span>
            <span className="list-item-meta">{formatDateTime(k.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No kills tracked yet</p>}
    </SectionCard>
  );
}

function DropsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.drops(playerId, {}, 100), [playerId]);
  return (
    <SectionCard title="Recent drops" accent="var(--series-3)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((d, i) => (
          <li key={i} className="list-item">
            <span className="list-item-name">
              {d.npcName && <MonsterIcon npcName={d.npcName} />}
              {d.quantity > 1 ? `${d.quantity}x ` : ""}
              {d.itemName}
              {d.npcName ? ` (${d.npcName})` : ""}
            </span>
            <span className="list-item-meta tabular-nums">{formatGp(d.value)} gp</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No drops tracked yet</p>}
    </SectionCard>
  );
}

function QuestsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.quests(playerId), [playerId]);
  return (
    <SectionCard title="Quests" accent="var(--series-6)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((q, i) => (
          <li key={i} className="list-item">
            <span>{titleCase(q.questName)}</span>
            <span className="list-item-meta">{formatDateTime(q.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No quests tracked yet</p>}
    </SectionCard>
  );
}

function SlayerTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.slayerTasks(playerId, {}, 100), [playerId]);
  return (
    <SectionCard title="Slayer tasks" accent="var(--series-4)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((t, i) => (
          <li key={i} className="list-item">
            <span>
              {t.taskName}
              {t.amountAssigned ? ` (${t.amountAssigned})` : ""}
            </span>
            <span className="list-item-meta">{t.completedAt ? formatDateTime(t.completedAt) : "in progress"}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No slayer tasks tracked yet</p>}
    </SectionCard>
  );
}

function FarmingTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.farmingPatches(playerId), [playerId]);
  return (
    <SectionCard title="Farming patches" accent="var(--series-6)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((f, i) => (
          <li key={i} className="list-item">
            <span>
              {f.patchName}
              {f.crop ? ` — ${f.crop}` : ""}
            </span>
            <span className="list-item-meta">{titleCase(f.state)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No farming patches tracked yet</p>}
    </SectionCard>
  );
}

function CollectionLogTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.collectionLog(playerId, {}, 200), [playerId]);
  return (
    <SectionCard title="Collection log unlocks" accent="var(--series-5)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((c, i) => (
          <li key={i} className="list-item">
            <span>{c.itemName}</span>
            <span className="list-item-meta">{formatDateTime(c.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No collection log items tracked yet</p>}
    </SectionCard>
  );
}

function CombatAchievementsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.combatAchievements(playerId, {}, 200), [playerId]);
  return (
    <SectionCard title="Combat achievements" accent="var(--series-7)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((c, i) => (
          <li key={i} className="list-item">
            <span>{c.taskName}</span>
            <span className="list-item-meta">{formatDateTime(c.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No combat achievements tracked yet</p>}
    </SectionCard>
  );
}

function DiariesTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.diaries(playerId), [playerId]);
  return (
    <SectionCard title="Achievement diaries" accent="var(--series-6)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((d, i) => (
          <li key={i} className="list-item">
            <span>
              {titleCase(d.diaryArea)} — {titleCase(d.tier)}
            </span>
            <span className="list-item-meta">{formatDateTime(d.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No diaries completed yet</p>}
    </SectionCard>
  );
}

function CluesTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.clues(playerId, {}, 200), [playerId]);
  return (
    <SectionCard title="Clue scrolls" accent="var(--series-5)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((c, i) => (
          <li key={i} className="list-item">
            <span>{titleCase(c.tier)}</span>
            <span className="list-item-meta">{formatDateTime(c.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No clue scrolls tracked yet</p>}
    </SectionCard>
  );
}

function PetsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.pets(playerId, {}, 200), [playerId]);
  return (
    <SectionCard title="Pets" accent="var(--series-8)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((p, i) => (
          <li key={i} className="list-item">
            <span>{p.petName ?? "Unknown pet"}</span>
            <span className="list-item-meta">{formatDateTime(p.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No pets tracked yet</p>}
    </SectionCard>
  );
}

function PersonalBestsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.personalBests(playerId), [playerId]);
  return (
    <SectionCard title="Personal bests" accent="var(--series-4)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((p, i) => (
          <li key={i} className="list-item">
            <span className="list-item-name">
              <MonsterIcon npcName={p.activityName} />
              {p.activityName}
            </span>
            <span className="list-item-meta tabular-nums">{formatDuration(p.durationSeconds)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No personal bests tracked yet</p>}
    </SectionCard>
  );
}

function PvpKillsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.playerKills(playerId, {}, 100), [playerId]);
  return (
    <SectionCard title="PvP kills" accent="var(--series-2)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((k, i) => (
          <li key={i} className="list-item">
            <span>{k.opponentName}</span>
            <span className="list-item-meta">{formatDateTime(k.ts)}</span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No PvP kills tracked yet</p>}
    </SectionCard>
  );
}

function PvpDeathsTab({ playerId }: { playerId: number }) {
  const { data, loading } = useApiData(() => api.playerDeaths(playerId, {}, 100), [playerId]);
  return (
    <SectionCard title="Deaths" accent="var(--series-8)">
      {loading && <p className="muted">Loading…</p>}
      <ul className="list">
        {(data ?? []).map((d, i) => (
          <li key={i} className="list-item">
            <span>Death</span>
            <span className="list-item-meta">
              {d.valueLost != null ? `${formatGp(d.valueLost)} gp — ` : ""}
              {formatDateTime(d.ts)}
            </span>
          </li>
        ))}
      </ul>
      {!loading && (data ?? []).length === 0 && <p className="muted">No deaths tracked yet</p>}
    </SectionCard>
  );
}
