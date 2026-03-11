# BC9 Attribution & Mapping — References

## BC8 Patterns Studied

### CanonicalBatch aggregate
- State machine: PROCESSING → PRODUCED/FAILED/REBUILT
- Deterministic UUID from SHA-256: `nameUUIDFromBytes(SHA-256(snapshotId:mappingVersion)[0..15])`
- Events: `CanonicalBatchStarted`, `CanonicalBatchProduced`, `CanonicalBatchFailed`, `CanonicalBatchRebuilt`
- `publishAndClear()` pattern in service layer

### NormalisationService orchestration
- Idempotency check first → create in initial state → save → publishAndClear
- Re-fetch aggregate before state transition (ensures latest DB state)
- Error handler: catch Exception → fail aggregate → save → publishAndClear → rethrow

### CanonicalBatchRepositoryImpl dual-interface
- Implements domain port + API port in single class
- `@Bean` in `ModuleRegistry` (not `@Repository`) to make cross-BC wiring explicit

## BC5 Patterns Studied

### GoogleConnectionRepositoryImpl
- Implements `GoogleConnectionRepository` + `GoogleAdsQueryPort`
- `listActiveBindings()` filters by `BindingStatus.ACTIVE`, maps entity → `AccountBindingInfo`

### AccountBindingEntity
- Simple JPA entity with `@CreationTimestamp` / `@UpdateTimestamp`
- Added `org_node_id UUID` and `org_scope_type VARCHAR(30)` columns (nullable)

## BC7 Patterns Studied

### RawCampaignRowQueryAdapter
- Adapter in ingestion module implementing BC8's `RawCampaignRowQueryPort`
- Simple `@Bean` in `ModuleRegistry`, no `@Component`/`@Repository` annotation
- Maps JPA entity fields to port record fields

## Shared Kernel

### EventEnvelope
- `ResolvableTypeProvider` ensures Spring routes events to correct typed `@EventListener`
- `EventEnvelope.of(eventType, payload)` factory for publishing

### ApiResponse
- `success(data, requestId)` / `error(code, message, requestId)` factory methods
- `requestId` extracted from `request.getAttribute("requestId")`
