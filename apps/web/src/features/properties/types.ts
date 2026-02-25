export interface PropertyListItem {
  id: string;
  name: string;
  isActive: boolean;
  externalPropertyRef: string | null;
  createdAt: string;
}

export interface CreatePropertyRequest {
  name: string;
  isActive: boolean;
  externalPropertyRef?: string;
}
