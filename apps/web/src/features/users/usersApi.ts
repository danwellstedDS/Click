import { apiRequest } from "../../lib/apiClient";
import type { CreateUserRequest, CreateUserResponse, UserDetail, UserListItem } from "./types";

export async function listUsers(): Promise<UserListItem[]> {
  return apiRequest<UserListItem[]>("/api/v1/users");
}

export async function createUser(req: CreateUserRequest): Promise<CreateUserResponse> {
  return apiRequest<CreateUserResponse>("/api/v1/users", {
    method: "POST",
    body: JSON.stringify(req),
  });
}

export async function getUser(id: string): Promise<UserDetail> {
  return apiRequest<UserDetail>(`/api/v1/users/${id}`);
}

export async function deleteUser(id: string): Promise<void> {
  await apiRequest<void>(`/api/v1/users/${id}`, { method: "DELETE" });
}
