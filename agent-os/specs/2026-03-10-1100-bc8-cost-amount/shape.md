# BC8 costAmount — Shaping Notes

## Problem

Downstream consumers of canonical facts (BC9, API clients) must compute `cost_micros / 1_000_000` to get a human-readable spend value. This is repetitive, error-prone (integer vs float division), and creates ergonomic drift as more consumers are added.

## Appetite

Small — single migration + 6 file touches. No new services, no new events.

## Solution

Add `cost_amount NUMERIC(18,6)` as a Postgres GENERATED ALWAYS AS STORED column. The DB computes it at insert time from `cost_micros`. All existing and future rows are correct by construction.

On the Java side:
- `CanonicalFactEntity` gets a read-only `costAmount` field (`insertable=false, updatable=false`).
- `CanonicalFactInfo` (BC8 API DTO) and `CanonicalFactData` (BC9 port) both expose `costAmount`.
- Mapper calls updated to thread `getCostAmount()` through.

## Out of Scope

- Removing `costMicros` — retained for precision and Google Ads API fidelity.
- Any changes to `Normalizer.java` — generated column is DB-side.
- BC10 / BC11 consumers — will pick up `costAmount` naturally from `CanonicalFactData`.
