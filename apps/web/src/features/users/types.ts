export type UserRole = "VIEWER" | "ANALYST" | "MANAGER" | "ADMIN" | "SUPPORT";

export interface UserListItem {
  id: string;
  email: string;
  role: UserRole;
  createdAt: string;
}

export interface MembershipInfo {
  tenantId: string;
  role: UserRole;
  memberSince: string;
}

export interface UserDetail extends UserListItem {
  updatedAt: string;
  memberships: MembershipInfo[];
}

export interface CreateUserRequest {
  email: string;
  role: UserRole;
}

export interface UpdateUserRoleRequest {
  role: UserRole;
}

export interface CreateUserResponse {
  user: UserListItem;
  temporaryPassword: string;
}
