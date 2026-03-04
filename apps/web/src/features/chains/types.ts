export type Chain = {
  id: string;
  name: string;
  status: 'ACTIVE' | 'INACTIVE';
  timezone: string | null;
  currency: string | null;
  primaryOrgId: string | null;
  createdAt: string;
  updatedAt: string;
};
