# Data Multitenancy Model

All tenant-owned entities include:

- `id` (UUID)
- `tenant_id`
- `created_at`
- `updated_at`

## Rules

- Composite uniqueness includes `tenant_id` where relevant
- Never allow reassignment across tenants via update
- Tenant isolation is mandatory in all repository methods
