import { apiRequest } from "../../lib/apiClient";
import type { CreatePropertyRequest, PropertyListItem } from "./types";

export async function listProperties(): Promise<PropertyListItem[]> {
  return apiRequest<PropertyListItem[]>("/api/v1/properties");
}

export async function createProperty(req: CreatePropertyRequest): Promise<PropertyListItem> {
  return apiRequest<PropertyListItem>("/api/v1/properties", {
    method: "POST",
    body: JSON.stringify(req),
  });
}

export async function deleteProperty(id: string): Promise<void> {
  await apiRequest<void>(`/api/v1/properties/${id}`, { method: "DELETE" });
}
