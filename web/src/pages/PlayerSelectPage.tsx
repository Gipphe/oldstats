import { usePlayer } from "../state/PlayerContext";
import "../styles/shared.css";

export function PlayerSelectPage() {
  const { players, selectedPlayer, loading, error, selectPlayer, refresh } = usePlayer();

  return (
    <div className="page player-select-page">
      <h1>Player</h1>
      {loading && <p className="muted">Loading players…</p>}
      {error && <p className="error">{error}</p>}
      {!loading && !error && players.length === 0 && (
        <p className="muted">
          No players registered yet. Register one from the server (<code>POST /api/players</code>) and
          configure the resulting API key in the OldStats RuneLite plugin, then refresh here.
        </p>
      )}
      <ul className="player-list">
        {players.map((p) => (
          <li key={p.id}>
            <button
              className={"player-list-item" + (selectedPlayer?.id === p.id ? " active" : "")}
              onClick={() => selectPlayer(p)}
            >
              <span>{p.username}</span>
              {selectedPlayer?.id === p.id && <span className="badge">Selected</span>}
            </button>
          </li>
        ))}
      </ul>
      <button className="refresh-button" onClick={refresh}>
        Refresh player list
      </button>
    </div>
  );
}
