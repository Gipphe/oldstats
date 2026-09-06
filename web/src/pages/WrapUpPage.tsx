import { useMemo, useState } from "react";
import { api } from "../api/client";
import { BarChart } from "../components/BarChart";
import { SectionCard } from "../components/SectionCard";
import { SkillIcon } from "../components/SkillIcon";
import { StatTile } from "../components/StatTile";
import { useApiData } from "../hooks/useApiData";
import { formatDate, formatDuration, formatGp, formatNumber, titleCase } from "../lib/format";
import { usePlayer } from "../state/PlayerContext";
import "../styles/shared.css";

const DAY_MS = 24 * 60 * 60 * 1000;

function weekRange(offset: number): { start: Date; end: Date } {
  const end = new Date(Date.now() - offset * 7 * DAY_MS);
  const start = new Date(end.getTime() - 7 * DAY_MS);
  return { start, end };
}

export function WrapUpPage() {
  const { selectedPlayer } = usePlayer();
  const [offset, setOffset] = useState(0);

  const { start, end } = useMemo(() => weekRange(offset), [offset]);

  const summary = useApiData(
    () =>
      selectedPlayer
        ? api.weeklySummary(selectedPlayer.id, start.toISOString(), end.toISOString())
        : Promise.reject(new Error("no player")),
    [selectedPlayer?.id, offset],
  );

  if (!selectedPlayer) {
    return (
      <div className="page">
        <h1>Weekly wrap-up</h1>
        <p className="muted">Select a player on the Player tab to see a wrap-up.</p>
      </div>
    );
  }

  const data = summary.data;

  return (
    <div className="page">
      <h1>Weekly wrap-up</h1>

      <div className="week-nav">
        <button onClick={() => setOffset((o) => o + 1)} aria-label="Previous week">
          ‹
        </button>
        <span>
          {formatDate(start.toISOString())} – {formatDate(end.toISOString())}
        </span>
        <button onClick={() => setOffset((o) => Math.max(0, o - 1))} disabled={offset === 0} aria-label="Next week">
          ›
        </button>
      </div>

      {summary.loading && <p className="muted">Loading…</p>}
      {summary.error && <p className="error">{summary.error}</p>}

      {data && (
        <>
          <SectionCard title="Highlights">
            <ul className="highlight-list">
              {data.highlights.map((h, i) => (
                <li key={i}>{h}</li>
              ))}
            </ul>
          </SectionCard>

          <div className="stat-grid">
            <StatTile tone="xp" label="XP gained" value={formatNumber(data.totalXpGained)} />
            <StatTile tone="combat" label="Kills" value={formatNumber(data.totalKills)} sublabel={`${data.bossKills} boss`} />
            <StatTile tone="economy" label="Loot value" value={`${formatGp(data.totalDropValue)} gp`} />
            <StatTile
              tone="boss"
              label="Slayer tasks"
              value={formatNumber(data.slayerTasksCompleted)}
              sublabel={data.topSlayerMonster ?? undefined}
            />
            <StatTile
              tone="combat"
              label="PvP"
              value={`${data.playerKills}-${data.playerDeaths}`}
              sublabel="kills-deaths"
            />
            <StatTile
              tone="economy"
              icon={<img src="/items/coins.png" alt="" width={22} height={22} />}
              label="Net worth"
              value={data.netWorthChange != null ? `${data.netWorthChange >= 0 ? "+" : ""}${formatGp(data.netWorthChange)} gp` : "—"}
            />
            <StatTile
              tone="world"
              label="Top world"
              value={data.topWorld ? `World ${data.topWorld.world}` : "—"}
              sublabel={data.topWorld ? `${Math.round(data.topWorld.minutes)}m` : undefined}
            />
          </div>

          <SectionCard title="Top skills" accent="var(--series-1)">
            <BarChart
              data={data.topSkills.map((s) => ({ label: titleCase(s.skill), value: s.xpGained, icon: <SkillIcon skill={s.skill} /> }))}
              emptyMessage="No XP this week"
            />
          </SectionCard>

          <SectionCard title="Most killed" accent="var(--series-2)">
            <BarChart
              data={data.topMonsters.map((m) => ({
                label: m.npcName,
                value: m.count,
                badge: m.isBoss ? "Boss" : undefined,
              }))}
              emptyMessage="No kills this week"
            />
          </SectionCard>

          <SectionCard title="Best drops" accent="var(--series-3)">
            {data.bestDrops.length === 0 ? (
              <p className="muted">No notable drops this week</p>
            ) : (
              <ul className="list">
                {data.bestDrops.map((drop, i) => (
                  <li key={i} className="list-item">
                    <span>{drop.itemName}</span>
                    <span className="list-item-meta tabular-nums">{formatGp(drop.value)} gp</span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Quests completed" accent="var(--series-6)">
            {data.questsCompleted.length === 0 ? (
              <p className="muted">No quests completed this week</p>
            ) : (
              <ul className="list">
                {data.questsCompleted.map((q, i) => (
                  <li key={i} className="list-item">
                    <span>{titleCase(q.questName)}</span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Farming" accent="var(--series-6)">
            <p className="muted">{data.farmingHarvests} patch(es) harvested this week</p>
          </SectionCard>

          <SectionCard title="Collection log" accent="var(--series-5)">
            {data.collectionLogUnlocks.length === 0 ? (
              <p className="muted">No new collection log items this week</p>
            ) : (
              <ul className="list">
                {data.collectionLogUnlocks.map((c, i) => (
                  <li key={i} className="list-item">
                    <span>{c.itemName}</span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Combat achievements" accent="var(--series-7)">
            {data.combatAchievementsCompleted.length === 0 ? (
              <p className="muted">No combat achievements completed this week</p>
            ) : (
              <>
                <ul className="list">
                  {data.combatAchievementsCompleted.map((c, i) => (
                    <li key={i} className="list-item">
                      <span>{c.taskName}</span>
                    </li>
                  ))}
                </ul>
                {data.combatAchievementPointsEarned > 0 && (
                  <p className="muted">+{data.combatAchievementPointsEarned} points earned</p>
                )}
              </>
            )}
          </SectionCard>

          <SectionCard title="Achievement diaries" accent="var(--series-6)">
            {data.diariesCompleted.length === 0 ? (
              <p className="muted">No diaries completed this week</p>
            ) : (
              <ul className="list">
                {data.diariesCompleted.map((d, i) => (
                  <li key={i} className="list-item">
                    <span>
                      {titleCase(d.diaryArea)} — {titleCase(d.tier)}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Clue scrolls" accent="var(--series-5)">
            {data.cluesCompleted.length === 0 ? (
              <p className="muted">No clue scrolls completed this week</p>
            ) : (
              <ul className="list">
                {data.cluesCompleted.map((c, i) => (
                  <li key={i} className="list-item">
                    <span>{titleCase(c.tier)}</span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Pets" accent="var(--series-8)">
            {data.petsReceived.length === 0 ? (
              <p className="muted">No pets received this week</p>
            ) : (
              <ul className="list">
                {data.petsReceived.map((p, i) => (
                  <li key={i} className="list-item">
                    <span>{p.petName ?? "Unknown pet"}</span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Personal bests" accent="var(--series-4)">
            {data.personalBests.length === 0 ? (
              <p className="muted">No new personal bests this week</p>
            ) : (
              <ul className="list">
                {data.personalBests.map((p, i) => (
                  <li key={i} className="list-item">
                    <span>{p.activityName}</span>
                    <span className="list-item-meta tabular-nums">{formatDuration(p.durationSeconds)}</span>
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
