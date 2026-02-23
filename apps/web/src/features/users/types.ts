export interface UserListItem {
  id: string;
  email: string;
  role: "ADMIN" | "VIEWER";
  createdAt: string;
}

export interface MembershipInfo {
  tenantId: string;
  role: "ADMIN" | "VIEWER";
  memberSince: string;
}

export interface UserDetail extends UserListItem {
  updatedAt: string;
  memberships: MembershipInfo[];
}

export interface CreateUserRequest {
  email: string;
  role: "ADMIN" | "VIEWER";
}

export interface CreateUserResponse {
  user: UserListItem;
  temporaryPassword: string;
}
