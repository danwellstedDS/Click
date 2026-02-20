import { Routes, Route } from "react-router-dom";
import { Typography } from "@derbysoft/neat-design";
import { LoginPage } from "./features/auth/LoginPage";
import { TenantPickerPage } from "./features/auth/TenantPickerPage";
import { PrivateRoute } from "./features/auth/PrivateRoute";

function Dashboard() {
  return (
    <div style={{ padding: 24 }}>
      <Typography.Title level={1}>Dashboard</Typography.Title>
      <Typography.Paragraph>Welcome! You are authenticated.</Typography.Paragraph>
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
