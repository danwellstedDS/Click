# Product Data Structure — Shaping Notes

## Problem

The auth-MVP schema is too thin for the product's actual requirements:
- `tenants` has no timezone, currency, or organization linkage
- Access control via `tenant_memberships` roles doesn't support multi-org, portfolio-level, or time-bounded grants
- No hotel/property model exists
- Billing is completely absent
- Single-org assumption is baked in everywhere

## Appetite

Large foundational task — replaces the core schema, domain layer, and persistence layer. This is a clean break, not an incremental migration. Known compile breakage in `apps/api` is acceptable and will be resolved in the subsequent auth refactor task.

## Solution

### Entity Graph

```
Chain (1) ─── (N) Hotel
Chain (1) ─── (N) Portfolio ─── (N) PortfolioHotel ─── (N) Hotel

Organization (1) ─── (N) OrgMembership ─── (N) User
Chain (N) ─── (1) Organization [primary_org_id]

AccessScope (type: CHAIN | HOTEL | PORTFOLIO)
  └─ chain_id always set
  └─ hotel_id set when type = HOTEL
  └─ portfolio_id set when type = PORTFOLIO

ScopeAccessGrant: Organization → AccessScope with GrantRole

CustomerAccount → Contract → ContractCoverage → AccessScope
```

### Access Control Model

Access is entirely grant-based:
1. User belongs to an Organization via `OrgMembership`
2. Organization has `ScopeAccessGrant` entries pointing to `AccessScope` records
3. Access scope covers a chain, hotel, or portfolio
4. No "managed_by" field on any entity determines access

### Billing Model

Billing is separate from authorization:
- `CustomerAccount` represents a billable entity (chain customer or agency customer)
- `Contract` tracks the agreement status and date range
- `ContractCoverage` links a contract to specific access scopes (what is covered)

## Scope

### In scope
- Database schema: all 9 migrations
- Domain layer: enums, models, repository interfaces
- Persistence layer: entities, JPA repos, impls, mappers
- Dev seed updated to new model

### Out of scope
- `apps/api` compile fix (auth refactor task)
- Access enforcement logic / middleware
- Billing service / billing API endpoints
- Portfolio management API endpoints
- Hotel management API endpoints

## Key Design Decisions

- **Portfolio included**: ScopeType includes CHAIN, HOTEL, PORTFOLIO
- **Clean break**: `tenant_memberships` dropped entirely, no migration path
- **GrantRole separate from Role**: `Role` (ADMIN/VIEWER) remains for JWT; `GrantRole` (VIEWER/ANALYST/MANAGER/ADMIN/SUPPORT) is the new access-level enum
- **Partial unique indexes**: NULL-safe uniqueness per scope type prevents duplicate scopes without nullable unique constraints
