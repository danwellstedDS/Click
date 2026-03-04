import { apiRequest } from "../../lib/apiClient";

export type OrgOption = { id: string; name: string; type: string };

export function list(): Promise<OrgOption[]> {
  return apiRequest<OrgOption[]>("/api/v1/organizations");
}
