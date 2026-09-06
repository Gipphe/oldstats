import { Fragment, useMemo, useState, type ComponentType, type CSSProperties, type MouseEvent, type ReactNode } from "react";
import { api } from "../api/client";
import { AnimatedNumber } from "../components/AnimatedNumber";
import { BarChart } from "../components/BarChart";
import { ItemIcon } from "../components/ItemIcon";
import { MonsterIcon } from "../components/MonsterIcon";
import { SkillIcon } from "../components/SkillIcon";
import { useApiData } from "../hooks/useApiData";
import { formatDate, formatDuration, formatGp, formatNumber, titleCase } from "../lib/format";
import { usePlayer } from "../state/PlayerContext";
import type { WeeklySummary } from "../api/types";
import "../styles/shared.css";
import "./WrapUpPage.css";

const DAY_MS = 24 * 60 * 60 * 1000;

function weekRange(offset: number): { start: Date; end: Date } {
  const end = new Date(Date.now() - offset * 7 * DAY_MS);
  const start = new Date(end.getTime() - 7 * DAY_MS);
  return { start, end };
}

interface SlideProps {
  data: WeeklySummary;
}

interface SlideDef {
  title: string;
  accent: string;
  Component: ComponentType<SlideProps>;
}

function RevealList({ children }: { children: ReactNode[] }) {
  return (
    <ul className="list">
      {children.map((child, i) => (
        <li key={i} className="list-item wrapup-reveal-item" style={{ animationDelay: `${i * 60}ms` }}>
          {child}
        </li>
      ))}
    </ul>
  );
}

function HighlightsSlide({ data }: SlideProps) {
  return (
    <>
      <p className="wrapup-slide-subtitle">Here's what happened.</p>
      <ul className="highlight-list">
        {data.highlights.map((h, i) => (
          <li key={i} className="wrapup-reveal-item" style={{ animationDelay: `${i * 70}ms` }}>
            {h}
          </li>
        ))}
      </ul>
    </>
  );
}

function SkillingSlide({ data }: SlideProps) {
  return (
    <>
      <div className="wrapup-hero">
        <span className="wrapup-hero-value">
          <AnimatedNumber value={data.totalXpGained} formatter={formatNumber} />
        </span>
        <span className="wrapup-hero-label">XP gained</span>
        {data.levelUps.length > 0 && (
          <span className="wrapup-hero-sublabel">Leveled up {data.levelUps.length} time{data.levelUps.length === 1 ? "" : "s"}</span>
        )}
      </div>
      <p className="wrapup-section-label">Top skills</p>
      <BarChart
        data={data.topSkills.map((s) => ({ label: titleCase(s.skill), value: s.xpGained, icon: <SkillIcon skill={s.skill} /> }))}
        emptyMessage="No XP this week"
      />
    </>
  );
}

function CombatSlide({ data }: SlideProps) {
  return (
    <>
      <div className="wrapup-hero">
        <span className="wrapup-hero-value">
          <AnimatedNumber value={data.totalKills} />
        </span>
        <span className="wrapup-hero-label">Kills</span>
        {data.bossKills > 0 && <span className="wrapup-hero-sublabel">{data.bossKills} boss kill{data.bossKills === 1 ? "" : "s"}</span>}
      </div>
      <p className="wrapup-section-label">Most killed</p>
      <BarChart
        data={data.topMonsters.map((m) => ({
          label: m.npcName,
          value: m.count,
          badge: m.isBoss ? "Boss" : undefined,
          icon: <MonsterIcon npcName={m.npcName} />,
        }))}
        emptyMessage="No kills this week"
      />
    </>
  );
}

function LootSlide({ data }: SlideProps) {
  return (
    <>
      <div className="wrapup-hero">
        <span className="wrapup-hero-value">
          <AnimatedNumber value={data.totalDropValue} formatter={(v) => `${formatGp(v)} gp`} />
        </span>
        <span className="wrapup-hero-label">Loot value</span>
      </div>
      {data.bestDrops.length > 0 && (
        <>
          <p className="wrapup-section-label">Best drops</p>
          <RevealList>
            {data.bestDrops.map((drop, i) => (
              <Fragment key={i}>
                <span className="list-item-name">
                  <ItemIcon itemName={drop.itemName} />
                  {drop.itemName}
                </span>
                <span className="list-item-meta tabular-nums">{formatGp(drop.value)} gp</span>
              </Fragment>
            ))}
          </RevealList>
        </>
      )}
      {data.bestDrops.length === 0 && <p className="muted">No notable drops this week</p>}
      <div className="wrapup-hero-row">
        <div className="wrapup-hero">
          <span className="wrapup-hero-label">Net worth</span>
          <span className="wrapup-hero-sublabel tabular-nums">
            {data.netWorthChange != null ? `${data.netWorthChange >= 0 ? "+" : ""}${formatGp(data.netWorthChange)} gp` : "—"}
          </span>
        </div>
        <div className="wrapup-hero">
          <span className="wrapup-hero-label">Top world</span>
          <span className="wrapup-hero-sublabel">{data.topWorld ? `World ${data.topWorld.world} (${Math.round(data.topWorld.minutes)}m)` : "—"}</span>
        </div>
      </div>
    </>
  );
}

function SlayerPvpSlide({ data }: SlideProps) {
  return (
    <>
      <div className="wrapup-hero-row">
        <div className="wrapup-hero">
          <span className="wrapup-hero-value">
            <AnimatedNumber value={data.slayerTasksCompleted} />
          </span>
          <span className="wrapup-hero-label">Slayer tasks</span>
          {data.topSlayerMonster && <span className="wrapup-hero-sublabel">mostly {data.topSlayerMonster}</span>}
          {data.slayerPointsEarned > 0 && <span className="wrapup-hero-sublabel">+{formatNumber(data.slayerPointsEarned)} points</span>}
        </div>
        <div className="wrapup-hero">
          <span className="wrapup-hero-value">
            {data.playerKills}-{data.playerDeaths}
          </span>
          <span className="wrapup-hero-label">PvP K/D</span>
        </div>
      </div>
    </>
  );
}

function ProgressionSlide({ data }: SlideProps) {
  const hasAny =
    data.questsCompleted.length > 0 || data.diariesCompleted.length > 0 || data.cluesCompleted.length > 0 || data.combatAchievementsCompleted.length > 0;
  return (
    <>
      {!hasAny && <p className="muted">No progression this week</p>}
      {data.questsCompleted.length > 0 && (
        <>
          <p className="wrapup-section-label">Quests completed</p>
          <RevealList>
            {data.questsCompleted.map((q, i) => (
              <span key={i}>{titleCase(q.questName)}</span>
            ))}
          </RevealList>
        </>
      )}
      {data.diariesCompleted.length > 0 && (
        <>
          <p className="wrapup-section-label">Achievement diaries</p>
          <RevealList>
            {data.diariesCompleted.map((d, i) => (
              <span key={i}>
                {titleCase(d.diaryArea)} — {titleCase(d.tier)}
              </span>
            ))}
          </RevealList>
        </>
      )}
      {data.cluesCompleted.length > 0 && (
        <>
          <p className="wrapup-section-label">Clue scrolls</p>
          <RevealList>
            {data.cluesCompleted.map((c, i) => (
              <span key={i}>{titleCase(c.tier)}</span>
            ))}
          </RevealList>
        </>
      )}
      {data.combatAchievementsCompleted.length > 0 && (
        <>
          <p className="wrapup-section-label">Combat achievements</p>
          <RevealList>
            {data.combatAchievementsCompleted.map((c, i) => (
              <span key={i}>{c.taskName}</span>
            ))}
          </RevealList>
          {data.combatAchievementPointsEarned > 0 && <p className="muted">+{data.combatAchievementPointsEarned} points earned</p>}
        </>
      )}
    </>
  );
}

function CollectiblesSlide({ data }: SlideProps) {
  const hasAny = data.collectionLogUnlocks.length > 0 || data.petsReceived.length > 0 || data.personalBests.length > 0 || data.farmingHarvests > 0;
  return (
    <>
      {!hasAny && <p className="muted">No collectibles this week</p>}
      {data.farmingHarvests > 0 && (
        <div className="wrapup-hero">
          <span className="wrapup-hero-value">
            <AnimatedNumber value={data.farmingHarvests} />
          </span>
          <span className="wrapup-hero-label">Patches harvested</span>
        </div>
      )}
      {data.collectionLogUnlocks.length > 0 && (
        <>
          <p className="wrapup-section-label">Collection log</p>
          <RevealList>
            {data.collectionLogUnlocks.map((c, i) => (
              <span key={i} className="list-item-name">
                <ItemIcon itemName={c.itemName} />
                {c.itemName}
              </span>
            ))}
          </RevealList>
        </>
      )}
      {data.petsReceived.length > 0 && (
        <>
          <p className="wrapup-section-label">Pets</p>
          <RevealList>
            {data.petsReceived.map((p, i) => (
              <span key={i}>{p.petName ?? "Unknown pet"}</span>
            ))}
          </RevealList>
        </>
      )}
      {data.personalBests.length > 0 && (
        <>
          <p className="wrapup-section-label">Personal bests</p>
          <RevealList>
            {data.personalBests.map((p, i) => (
              <Fragment key={i}>
                <span className="list-item-name">
                  <MonsterIcon npcName={p.activityName} />
                  {p.activityName}
                </span>
                <span className="list-item-meta tabular-nums">{formatDuration(p.durationSeconds)}</span>
              </Fragment>
            ))}
          </RevealList>
        </>
      )}
    </>
  );
}

const SLIDES: SlideDef[] = [
  { title: "Your Week", accent: "var(--series-1)", Component: HighlightsSlide },
  { title: "Skilling", accent: "var(--series-1)", Component: SkillingSlide },
  { title: "Combat", accent: "var(--series-2)", Component: CombatSlide },
  { title: "Loot & Wealth", accent: "var(--series-3)", Component: LootSlide },
  { title: "Slayer & PvP", accent: "var(--series-4)", Component: SlayerPvpSlide },
  { title: "Quests & Progression", accent: "var(--series-6)", Component: ProgressionSlide },
  { title: "Collectibles", accent: "var(--series-5)", Component: CollectiblesSlide },
];

function WrapUpStage({ data }: { data: WeeklySummary }) {
  const [slideIndex, setSlideIndex] = useState(0);

  const goPrev = () => setSlideIndex((i) => Math.max(0, i - 1));
  const goNext = () => setSlideIndex((i) => Math.min(SLIDES.length - 1, i + 1));

  function handleTap(e: MouseEvent<HTMLDivElement>) {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX - rect.left;
    if (x < rect.width * 0.35) goPrev();
    else goNext();
  }

  const active = SLIDES[slideIndex];
  const Slide = active.Component;

  return (
    <div className="wrapup-stage">
      <div className="wrapup-dots">
        {SLIDES.map((s, i) => (
          <button
            key={s.title}
            className={"wrapup-dot" + (i <= slideIndex ? " filled" : "") + (i === slideIndex ? " current" : "")}
            onClick={() => setSlideIndex(i)}
            aria-label={`Go to ${s.title}`}
          />
        ))}
      </div>

      <div className="wrapup-slide-viewport" onClick={handleTap}>
        <div className="wrapup-slide" key={slideIndex} style={{ "--slide-accent": active.accent } as CSSProperties}>
          <h2 className="wrapup-slide-title">{active.title}</h2>
          <Slide data={data} />
        </div>
      </div>

      <div className="wrapup-slide-nav">
        <button
          onClick={(e) => {
            e.stopPropagation();
            goPrev();
          }}
          disabled={slideIndex === 0}
        >
          ‹ Back
        </button>
        <span className="wrapup-slide-counter">
          {slideIndex + 1} / {SLIDES.length}
        </span>
        <button
          onClick={(e) => {
            e.stopPropagation();
            goNext();
          }}
          disabled={slideIndex === SLIDES.length - 1}
        >
          Next ›
        </button>
      </div>
    </div>
  );
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

  const data = summary.data;

  if (!selectedPlayer) {
    return (
      <div className="page">
        <h1>Weekly wrap-up</h1>
        <p className="muted">Select a player on the Player tab to see a wrap-up.</p>
      </div>
    );
  }

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

      {data && <WrapUpStage key={offset} data={data} />}
    </div>
  );
}
