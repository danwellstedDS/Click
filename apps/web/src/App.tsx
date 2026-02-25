import { Routes, Route } from "react-router-dom";
import { LoginPage } from "./features/auth/LoginPage";
import { TenantPickerPage } from "./features/auth/TenantPickerPage";
import { PrivateRoute } from "./features/auth/PrivateRoute";
import { AdminRoute } from "./features/auth/AdminRoute";
import { AppLayout } from "./components/AppLayout";
import { UsersListPage } from "./features/users/UsersListPage";
import { UserDetailPage } from "./features/users/UserDetailPage";
import { PropertiesListPage } from "./features/properties/PropertiesListPage";

function Dashboard() {
  return (
    <AppLayout title="Overview Performance Dashboard" breadcrumb="Dashboard">
      {/* placeholder — real content comes later */}
    </AppLayout>
  );
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/select-tenant" element={<TenantPickerPage />} />
      <Route
        path="/properties"
        element={
          <PrivateRoute>
            <AdminRoute>
              <PropertiesListPage />
            </AdminRoute>
          </PrivateRoute>
        }
      />
      <Route
        path="/users"
        element={
          <PrivateRoute>
            <AdminRoute>
              <UsersListPage />
            </AdminRoute>
          </PrivateRoute>
        }
      />
      <Route
        path="/users/:id"
        element={
          <PrivateRoute>
            <AdminRoute>
              <UserDetailPage />
            </AdminRoute>
          </PrivateRoute>
        }
      />
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
