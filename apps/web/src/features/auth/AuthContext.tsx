import React, { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "./authApi";
import type { TenantSummary } from "./authApi";
import type { MeResponse, TenantMembership } from "./types";

interface AuthState {
  user: MeResponse | null;
  tenants: TenantSummary[];
  isLoading: boolean;
  login: (email: string, password: string) => Promise<TenantMembership[]>;
  logout: () => Promise<void>;
  switchTenant: (tenantId: string) => Promise<void>;
  refreshTenants: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<MeResponse | null>(null);
  const [tenants, setTenants] = useState<TenantSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    Promise.all([authApi.me(), authApi.listTenants()])
      .then(([profile, tenantList]) => {
        setUser(profile);
        setTenants(tenantList);
      })
      .catch(() => {
        setUser(null);
        setTenants([]);
      })
      .finally(() => setIsLoading(false));
  }, []);

  async function login(email: string, password: string): Promise<TenantMembership[]> {
    const response = await authApi.login(email, password);
    const [profile, tenantList] = await Promise.all([authApi.me(), authApi.listTenants()]);
    setUser(profile);
    setTenants(tenantList);
    return response.tenants;
  }

  async function logout() {
    await authApi.logout();
    setUser(null);
    setTenants([]);
  }

  async function switchTenant(tenantId: string) {
    await authApi.switchTenant(tenantId);
    const [profile, tenantList] = await Promise.all([authApi.me(), authApi.listTenants()]);
    setUser(profile);
    setTenants(tenantList);
  }

  async function refreshTenants() {
    const tenantList = await authApi.listTenants();
    setTenants(tenantList);
  }

  return (
    <AuthContext.Provider value={{ user, tenants, isLoading, login, logout, switchTenant, refreshTenants }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
