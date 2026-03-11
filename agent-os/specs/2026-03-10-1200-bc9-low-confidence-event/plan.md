# BC9 — LowConfidenceMappingDetected Event

## Goal

Emit a dedicated `LowConfidenceMappingDetected` domain event once per mapping run when
`lowConfidenceCount > 0 || unresolvedCount > 0`, after `MappingResultBatchProduced`.

This gives BC10 and monitoring workflows a targeted event containing the individual fact IDs
without re-querying the mapped facts table.

## Changes

| File | Change |
|------|--------|
| `attributionmapping/domain/events/LowConfidenceMappingDetected.java` | New event record |
| `attributionmapping/domain/aggregates/MappingRun.java` | Updated `produce()` signature + conditional emit |
| `attributionmapping/application/handlers/AttributionService.java` | Collect `lowConfidenceFactIds`, pass to `produce()` |
| `attributionmapping/domain/MappingRunTest.java` | Updated call sites + 2 new tests |
| `attributionmapping/application/AttributionServiceTest.java` | Fix `costAmount` in `CanonicalFactData` constructor |
| `docs/todo.md` | Mark gap done |

## Side-fix

`AttributionServiceTest.fact()` was broken by the BC8 `costAmount` field added to `CanonicalFactData`.
Fixed by inserting `BigDecimal.valueOf(0.5)` before `conversions`.
