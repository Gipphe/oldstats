import { usePlayer } from "../state/PlayerContext";
import "./AppHeader.css";

export function AppHeader() {
  const { selectedPlayer } = usePlayer();

  return (
    <header className="app-header">
      <svg className="app-header-emblem" viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
        <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" strokeWidth="1.4" />
        <path d="M12 4l2.2 5.8L20 12l-5.8 2.2L12 20l-2.2-5.8L4 12l5.8-2.2L12 4z" fill="currentColor" />
      </svg>
      <span className="app-header-title">OldStats</span>
      {selectedPlayer && <span className="app-header-player">{selectedPlayer.username}</span>}
    </header>
  );
}
