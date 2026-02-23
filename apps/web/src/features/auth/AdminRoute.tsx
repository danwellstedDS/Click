import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

interface AdminRouteProps {
  children: React.ReactNode;
}

export function AdminRoute({ children }: AdminRouteProps) {
  const { user } = useAuth();

  if (user?.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
