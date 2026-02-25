export interface UserProfile {
  id: string;
  email: string;
}

export interface TenantMembership {
  tenantId: string;
  role: "ADMIN" | "VIEWER";
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: UserProfile;
  tenants: TenantMembership[];
}

export interface MeResponse {
  id: string;
  email: string;
  tenantId: string;
  tenantName: string;
  role: "ADMIN" | "VIEWER";
}

export interface TokenResponse {
  token: string;
}
