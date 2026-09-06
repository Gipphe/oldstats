import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api } from "../api/client";
import type { Player } from "../api/types";

const STORAGE_KEY = "oldstats.selectedPlayerId";

interface PlayerContextValue {
  players: Player[];
  selectedPlayer: Player | null;
  loading: boolean;
  error: string | null;
  selectPlayer: (player: Player) => void;
  refresh: () => void;
}

const PlayerContext = createContext<PlayerContextValue | null>(null);

export function PlayerProvider({ children }: { children: ReactNode }) {
  const [players, setPlayers] = useState<Player[]>([]);
  const [selectedPlayer, setSelectedPlayer] = useState<Player | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    api
      .listPlayers()
      .then((list) => {
        if (cancelled) return;
        setPlayers(list);
        const storedId = localStorage.getItem(STORAGE_KEY);
        const stored = storedId ? list.find((p) => String(p.id) === storedId) : undefined;
        if (stored) {
          setSelectedPlayer(stored);
        } else if (list.length === 1) {
          setSelectedPlayer(list[0]);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : "Failed to load players");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [refreshToken]);

  const selectPlayer = (player: Player) => {
    setSelectedPlayer(player);
    localStorage.setItem(STORAGE_KEY, String(player.id));
  };

  const value = useMemo<PlayerContextValue>(
    () => ({
      players,
      selectedPlayer,
      loading,
      error,
      selectPlayer,
      refresh: () => setRefreshToken((t) => t + 1),
    }),
    [players, selectedPlayer, loading, error],
  );

  return <PlayerContext.Provider value={value}>{children}</PlayerContext.Provider>;
}

export function usePlayer(): PlayerContextValue {
  const ctx = useContext(PlayerContext);
  if (!ctx) throw new Error("usePlayer must be used within a PlayerProvider");
  return ctx;
}
