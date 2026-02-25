import { Routes, Route } from "react-router-dom";
import { LoginPage } from "./features/auth/LoginPage";
import { TenantPickerPage } from "./features/auth/TenantPickerPage";
import { PrivateRoute } from "./features/auth/PrivateRoute";
import { AdminRoute } from "./features/auth/AdminRoute";
import { UsersListPage } from "./features/users/UsersListPage";
import { UserDetailPage } from "./features/users/UserDetailPage";
import { PropertiesListPage } from "./features/properties/PropertiesListPage";
import { DashboardPage } from "./features/dashboard/DashboardPage";

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
            <DashboardPage />
          </PrivateRoute>
        }
      />
    </Routes>
  );
}
