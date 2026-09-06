import { Navigate, Route, Routes } from "react-router-dom";
import { BottomNav } from "./components/BottomNav";
import { ActivityPage } from "./pages/ActivityPage";
import { BankPage } from "./pages/BankPage";
import { DashboardPage } from "./pages/DashboardPage";
import { PlayerSelectPage } from "./pages/PlayerSelectPage";
import { WrapUpPage } from "./pages/WrapUpPage";

export function App() {
  return (
    <div className="app-shell">
      <main className="app-content">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/wrap-up" element={<WrapUpPage />} />
          <Route path="/activity" element={<ActivityPage />} />
          <Route path="/bank" element={<BankPage />} />
          <Route path="/player" element={<PlayerSelectPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <BottomNav />
    </div>
  );
}
