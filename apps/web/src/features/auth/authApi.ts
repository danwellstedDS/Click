import { apiRequest } from "../../lib/apiClient";
import type { LoginResponse, MeResponse, TokenResponse } from "./types";

export async function login(email: string, password: string): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export async function logout(): Promise<void> {
  await apiRequest<{ message: string }>("/api/v1/auth/logout", { method: "POST" });
}

export async function me(): Promise<MeResponse> {
  return apiRequest<MeResponse>("/api/v1/auth/me");
}

export async function switchTenant(tenantId: string): Promise<TokenResponse> {
  return apiRequest<TokenResponse>("/api/v1/auth/switch-tenant", {
    method: "POST",
    body: JSON.stringify({ tenantId }),
  });
}

export async function refresh(): Promise<TokenResponse> {
  return apiRequest<TokenResponse>("/api/v1/auth/refresh", { method: "POST" });
}
