import { apiRequest } from "../../lib/apiClient";
import type { Chain } from "./types";

export function list(): Promise<Chain[]> {
  return apiRequest<Chain[]>("/api/v1/chains");
}

export function create(data: { name: string; timezone?: string; currency?: string; organizationId?: string }): Promise<Chain> {
  return apiRequest<Chain>("/api/v1/chains", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function updateStatus(id: string, status: "ACTIVE" | "INACTIVE"): Promise<Chain> {
  return apiRequest<Chain>(`/api/v1/chains/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });
}
