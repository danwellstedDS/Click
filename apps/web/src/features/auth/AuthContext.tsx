import React, { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "./authApi";
import type { MeResponse, TenantMembership } from "./types";

interface AuthState {
  user: MeResponse | null;
  tenants: TenantMembership[];
  isLoading: boolean;
  login: (email: string, password: string) => Promise<TenantMembership[]>;
  logout: () => Promise<void>;
  switchTenant: (tenantId: string) => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<MeResponse | null>(null);
  const [tenants, setTenants] = useState<TenantMembership[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    authApi
      .me()
      .then((profile) => setUser(profile))
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false));
  }, []);

  async function login(email: string, password: string): Promise<TenantMembership[]> {
    const response = await authApi.login(email, password);
    setTenants(response.tenants);
    const profile = await authApi.me();
    setUser(profile);
    return response.tenants;
  }

  async function logout() {
    await authApi.logout();
    setUser(null);
    setTenants([]);
  }

  async function switchTenant(tenantId: string) {
    await authApi.switchTenant(tenantId);
    const profile = await authApi.me();
    setUser(profile);
  }

  return (
    <AuthContext.Provider value={{ user, tenants, isLoading, login, logout, switchTenant }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
