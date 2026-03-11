# BC8 — Add costAmount to Canonical Facts

## Context

The BC8 spec requires canonical facts to persist both `costMicros` (raw integer from Google Ads) and `costAmount` (human-readable decimal = `costMicros / 1_000_000`). Previously `CanonicalFactEntity` stored only `costMicros`, forcing downstream consumers to divide by 1,000,000 themselves — causing ergonomics drift and potential for inconsistent rounding.

Adding `costAmount` as a Postgres generated stored column eliminates drift entirely: the DB always computes it from `costMicros`.

## Changes

| File | Change |
|------|--------|
| `V202603100001__add_cost_amount_to_canonical_facts.sql` | Create — generated column migration |
| `CanonicalFactEntity.java` | Add `costAmount` field + getter |
| `CanonicalFactInfo.java` | Add `costAmount` to record |
| `CanonicalBatchRepositoryImpl.java` | Pass `getCostAmount()` in `toFactInfo()` |
| `CanonicalFactQueryPort.java` | Add `costAmount` to `CanonicalFactData` |
| `CanonicalFactQueryAdapter.java` | Pass `getCostAmount()` in `toData()` |
| `docs/todo.md` | Mark BC8 compliance gap #5 done |

## Notes

- `insertable = false, updatable = false` on the entity field is required for Postgres generated columns.
- `Normalizer.java` — no change needed; generated column is computed by DB at insert time.
- No backfill needed — STORED generated column is computed automatically for all existing rows on migration.
