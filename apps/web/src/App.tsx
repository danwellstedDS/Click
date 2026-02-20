import { Routes, Route } from "react-router-dom";
import { LoginPage } from "./features/auth/LoginPage";
import { TenantPickerPage } from "./features/auth/TenantPickerPage";
import { PrivateRoute } from "./features/auth/PrivateRoute";

function Dashboard() {
  return (
    <div style={{ fontFamily: "system-ui", padding: 24 }}>
      <h1>Dashboard</h1>
      <p>Welcome! You are authenticated.</p>
    </div>
  );
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/select-tenant" element={<TenantPickerPage />} />
      <Route
        path="/*"
        element={
          <PrivateRoute>
            <Dashboard />
          </PrivateRoute>
        }
      />
    </Routes>
  );
}
