import { api } from "../api/client";
import { BarChart } from "../components/BarChart";
import { LineChart } from "../components/LineChart";
import { SectionCard } from "../components/SectionCard";
import { StatTile } from "../components/StatTile";
import { useApiData } from "../hooks/useApiData";
import { formatGp, formatNumber, titleCase } from "../lib/format";
import { usePlayer } from "../state/PlayerContext";
import "../styles/shared.css";

export function DashboardPage() {
  const { selectedPlayer } = usePlayer();

  const overview = useApiData(
    () => (selectedPlayer ? api.overview(selectedPlayer.id) : Promise.reject(new Error("no player"))),
    [selectedPlayer?.id],
  );
  const xpBySkill = useApiData(
    () => (selectedPlayer ? api.xpBySkill(selectedPlayer.id) : Promise.reject(new Error("no player"))),
    [selectedPlayer?.id],
  );
  const killsByNpc = useApiData(
    () => (selectedPlayer ? api.killsByNpc(selectedPlayer.id) : Promise.reject(new Error("no player"))),
    [selectedPlayer?.id],
  );
  const worlds = useApiData(
    () => (selectedPlayer ? api.worlds(selectedPlayer.id) : Promise.reject(new Error("no player"))),
    [selectedPlayer?.id],
  );
  const netWorth = useApiData(
    () => (selectedPlayer ? api.netWorth(selectedPlayer.id, {}, 200) : Promise.reject(new Error("no player"))),
    [selectedPlayer?.id],
  );

  if (!selectedPlayer) {
    return (
      <div className="page">
        <h1>Dashboard</h1>
        <p className="muted">Select a player on the Player tab to see stats.</p>
      </div>
    );
  }

  const kdr =
    overview.data && overview.data.playerDeaths > 0
      ? (overview.data.playerKills / overview.data.playerDeaths).toFixed(2)
      : overview.data?.playerKills ? overview.data.playerKills.toString() : "—";

  return (
    <div className="page">
      <h1>{selectedPlayer.username}</h1>

      <div className="stat-grid">
        <StatTile tone="xp" label="Total XP" value={overview.data ? formatNumber(overview.data.totalXpGained) : "—"} />
        <StatTile tone="combat" label="Kills" value={overview.data ? formatNumber(overview.data.totalKills) : "—"} />
        <StatTile tone="boss" label="Boss kills" value={overview.data ? formatNumber(overview.data.totalBossKills) : "—"} />
        <StatTile tone="economy" label="Loot value" value={overview.data ? `${formatGp(overview.data.totalDropValue)} gp` : "—"} />
        <StatTile tone="progression" label="Quests done" value={overview.data ? formatNumber(overview.data.questsCompleted) : "—"} />
        <StatTile tone="boss" label="Slayer tasks" value={overview.data ? formatNumber(overview.data.slayerTasksCompleted) : "—"} />
        <StatTile
          tone="collectible"
          label="Collection log"
          value={overview.data ? formatNumber(overview.data.collectionLogTotalUnlocked) : "—"}
          sublabel={overview.data?.collectionLogTotalPossible ? `of ${overview.data.collectionLogTotalPossible}` : undefined}
        />
        <StatTile
          tone="prestige"
          label="Combat achievements"
          value={overview.data ? formatNumber(overview.data.combatAchievementsCompleted) : "—"}
          sublabel={overview.data ? `${formatNumber(overview.data.combatAchievementPoints)} points` : undefined}
        />
        <StatTile tone="progression" label="Diaries completed" value={overview.data ? formatNumber(overview.data.diariesCompleted) : "—"} />
        <StatTile tone="collectible" label="Clues completed" value={overview.data ? formatNumber(overview.data.cluesCompleted) : "—"} />
        <StatTile tone="companion" label="Pets received" value={overview.data ? formatNumber(overview.data.petsReceived) : "—"} />
        <StatTile
          tone="combat"
          label="PvP K/D"
          value={kdr}
          sublabel={overview.data ? `${overview.data.playerKills} kills, ${overview.data.playerDeaths} deaths` : undefined}
        />
        <StatTile tone="economy" label="Net worth" value={overview.data?.netWorth != null ? `${formatGp(overview.data.netWorth)} gp` : "—"} />
      </div>

      <SectionCard title="XP by skill" accent="var(--series-1)">
        <BarChart
          data={(xpBySkill.data ?? []).slice(0, 8).map((s) => ({ label: titleCase(s.skill), value: s.xpGained }))}
          emptyMessage="No XP tracked yet"
        />
      </SectionCard>

      <SectionCard title="Most killed" accent="var(--series-2)">
        <BarChart
          data={(killsByNpc.data ?? []).slice(0, 8).map((k) => ({
            label: k.npcName,
            value: k.count,
            badge: k.isBoss ? "Boss" : undefined,
          }))}
          emptyMessage="No kills tracked yet"
        />
      </SectionCard>

      <SectionCard title="Net worth over time" accent="var(--series-3)">
        <LineChart
          data={(netWorth.data ?? [])
            .slice()
            .reverse()
            .map((n) => ({ ts: n.ts, value: n.totalValue }))}
          valueFormatter={(v) => `${formatGp(v)} gp`}
          emptyMessage="No net worth snapshots yet"
        />
      </SectionCard>

      <SectionCard title="Worlds played" accent="var(--series-7)">
        <BarChart
          data={(worlds.data ?? []).slice(0, 8).map((w) => ({
            label: `World ${w.world}`,
            value: w.minutes,
            badge: w.worldTypes.includes("PVP") ? "PvP" : undefined,
          }))}
          valueFormatter={(v) => `${Math.round(v)}m`}
          emptyMessage="No world data tracked yet"
        />
      </SectionCard>
    </div>
  );
}
