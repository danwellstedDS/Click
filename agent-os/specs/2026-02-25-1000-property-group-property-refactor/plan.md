# PropertyGroup + Property Refactor — Plan

**Date:** 2026-02-25
**Spec ID:** 2026-02-25-1000-property-group-property-refactor

## Problem

The existing data model uses `Chain` as the top-level entity owning `Hotel` records. This is too rigid:
- No ability to nest groups of properties hierarchically.
- Naming (`Chain`, `Hotel`) is domain-specific and limits reuse outside hotel chains.

## Solution

Replace `Chain` with `PropertyGroup` (self-referential, up to 5 levels deep) and rename `Hotel` to `Property`. Update all dependent entities, repositories, services, and tests accordingly.

## Scope

- DB: Drop old tables (chains, hotels, portfolio_hotels) and recreate as property_groups, properties, portfolio_properties.
- Domain: Rename Chain→PropertyGroup, Hotel→Property, PortfolioHotel→PortfolioProperty; update ScopeType enum values.
- Persistence: Rename/update all entities, mappers, JPA repositories, and repository implementations.
- Application: Update AuthService and UserManagementService imports and variable names.
- Frontend: Update sidebar nav item Hotels→Properties.
- Tests: Update AuthServiceTest mocks.

## Not in Scope

- Agency or DerbySoft organization type changes.
- Billing (contracts, customer accounts) — no FK changes needed.
- Adding property group hierarchy enforcement logic (depth limit is a DB/app concern for future).
