# Applicable Standards

This spec follows the project standards defined in the following files:

## Backend Standards
Path: `agent-os/standards/backend/standards.md`

Key rules applied in this feature:
- Layered DDD architecture: Presentation → Application → Domain → Persistence
- Repository interfaces in `libs/domain` (no Spring/JPA annotations)
- JPA implementations in `libs/persistence`
- Constructor injection only (no `@Autowired` field injection)
- Enum columns use `@Enumerated(EnumType.STRING)` in entities
- `@UuidGenerator` for primary keys
- `@CreationTimestamp` / `@UpdateTimestamp` for timestamp columns
- Mappers are static utility classes with `toDomain(Entity)` method
- `record` types for simple domain value objects (OrgMembership, PortfolioHotel, AccessScope, ScopeAccessGrant, ContractCoverage)
- Full class pattern (private constructor + static factory) for mutable aggregate roots (Chain, Hotel, Organization, Portfolio, CustomerAccount, Contract)

## Database Standards

- All tables use `UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- Timestamps use `TIMESTAMPTZ NOT NULL DEFAULT now()`
- Enum columns stored as `TEXT NOT NULL CHECK (value IN (...))`
- Partial unique indexes for NULL-safe scope uniqueness
- Foreign keys use `ON DELETE CASCADE` for owned relationships, `ON DELETE SET NULL` for optional references, `ON DELETE RESTRICT` for billing references
- All indexes named: `idx_<table>_<column(s)>`

## Workflow Standards
Path: `agent-os/standards/workflow/standards.md`

Branch naming: `feature/product-data-structure-basis`
Commit convention: `feat(data): replace tenant model with full product data structure`
