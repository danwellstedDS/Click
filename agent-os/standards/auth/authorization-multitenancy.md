# Authorization & Multitenancy

Multi-tenancy is enforced everywhere.

- `tenantId` is derived from token claims
- Never accept `tenantId` from request payload
- All tenant-owned tables include `tenant_id`
- All DB queries require `tenantId`
- Cross-tenant access returns `AUTH_403`

## Hierarchy

- Tenant → Chain → Hotel
- Chain belongs to one Tenant
- Hotel belongs to one Chain
