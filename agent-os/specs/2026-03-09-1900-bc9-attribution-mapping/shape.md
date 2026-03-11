# BC9 Attribution & Mapping — Shape Notes

## Problem
BC8 emits canonical campaign-day facts with no org-node attachment. BC9 closes this gap so every fact knows which Property or PropertyGroup it belongs to — enabling BC10 (Reporting Portfolio) to aggregate by org hierarchy.

## Key Decisions

### Strict precedence model
1. Manual override (ACCOUNT_CAMPAIGN > ACCOUNT scope)
2. Explicit BC5 binding (requires `org_node_id` to be populated)
3. UNRESOLVED (queued for human review)

No heuristics in MVP-1. Confidence is binary: HIGH (1.0 override, 0.9 binding) or UNRESOLVED (0.0). MEDIUM/LOW reserved for MVP-2 fuzzy matching.

### Idempotency key
`SHA-256(canonicalBatchId:ruleSetVersion:overrideSetVersion)` → deterministic UUID. Override-set versioning means a change in active overrides produces a new run against the same batch — deliberate re-attribution.

### BC5 pre-condition
`account_bindings.org_node_id` is nullable and unpopulated at migration time. BC9 runs silently with all UNRESOLVED until operator populates org_node_id for each binding. This is by design — no blocking dependency.

### Dual-interface pattern
`MappingRunRepositoryImpl` implements both `MappingRunRepository` (domain) and `AttributionQueryPort` (public API) — same pattern as BC8's `CanonicalBatchRepositoryImpl`.

## Scope Boundary
MVP-1: explicit binding + manual overrides + REST read/write API.
MVP-2 (out of scope): similarity matching (MEDIUM), property-name heuristics (LOW), bulk override import.
