import { NavLink } from "react-router-dom";
import "./BottomNav.css";

const items = [
  { to: "/", label: "Dashboard", icon: <path d="M4 13h7V4H4v9zm0 7h7v-5H4v5zm9 0h7V11h-7v9zm0-16v5h7V4h-7z" /> },
  { to: "/wrap-up", label: "Wrap-up", icon: <path d="M12 2 3 7v6c0 5 4 8.5 9 9 5-.5 9-4 9-9V7l-9-5z" /> },
  {
    to: "/activity",
    label: "Activity",
    icon: <path d="M4 5h16v2H4V5zm0 6h16v2H4v-2zm0 6h10v2H4v-2z" />,
  },
  { to: "/player", label: "Player", icon: <path d="M12 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10zm0 2c-4.4 0-8 2.2-8 5v3h16v-3c0-2.8-3.6-5-8-5z" /> },
];

export function BottomNav() {
  return (
    <nav className="bottom-nav">
      {items.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.to === "/"}
          className={({ isActive }) => "bottom-nav-item" + (isActive ? " active" : "")}
        >
          <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor" aria-hidden="true">
            {item.icon}
          </svg>
          <span>{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}
