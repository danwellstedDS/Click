# PropertyGroup + Property Refactor — Data Shape

## Table: property_groups (replaces chains)

| Column        | Type        | Notes                                         |
|---------------|-------------|-----------------------------------------------|
| id            | UUID PK     | gen_random_uuid()                             |
| parent_id     | UUID FK     | self-ref → property_groups(id), nullable      |
| name          | TEXT NN     |                                               |
| timezone      | TEXT        |                                               |
| currency      | TEXT        |                                               |
| primary_org_id| UUID FK     | → organizations(id), nullable                 |
| created_at    | TIMESTAMPTZ |                                               |
| updated_at    | TIMESTAMPTZ |                                               |

## Table: properties (replaces hotels)

| Column               | Type        | Notes                                    |
|----------------------|-------------|------------------------------------------|
| id                   | UUID PK     |                                          |
| property_group_id    | UUID FK NN  | → property_groups(id)                    |
| name                 | TEXT NN     |                                          |
| is_active            | BOOLEAN NN  | default TRUE                             |
| external_property_ref| TEXT        |                                          |
| created_at           | TIMESTAMPTZ |                                          |
| updated_at           | TIMESTAMPTZ |                                          |

## Table: portfolios (FK renamed)

- `chain_id` → `property_group_id` (references property_groups)
- Unique constraint: (property_group_id, name)

## Table: portfolio_properties (replaces portfolio_hotels)

| Column       | Type       | Notes                      |
|--------------|------------|----------------------------|
| id           | UUID PK    |                            |
| portfolio_id | UUID FK NN | → portfolios(id)           |
| property_id  | UUID FK NN | → properties(id)           |

## Table: access_scopes (column renames)

- `chain_id` → `property_group_id` (NOT NULL)
- `hotel_id` → `property_id` (nullable)
- Type enum: CHAIN→PROPERTY_GROUP, HOTEL→PROPERTY

## Enum: ScopeType

| Old   | New            |
|-------|----------------|
| CHAIN | PROPERTY_GROUP |
| HOTEL | PROPERTY       |
| PORTFOLIO | PORTFOLIO  |

## Domain Hierarchy

```
PropertyGroup (root, parent_id = NULL)
  └── PropertyGroup (child, parent_id = root.id)
        └── Property (leaf)
```

Up to 5 levels of nesting (enforced by application layer in future).
