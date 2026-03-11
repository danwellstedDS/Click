# BC9 Attribution & Mapping — Standards Applied

## DDD / Hexagonal Architecture
- Domain aggregates (`MappingRun`, `MappingOverride`) own state-machine logic and emit domain events
- Repository interfaces live in the domain package; implementations in infrastructure
- Application services orchestrate; domain objects enforce invariants

## Event-Driven Integration
- `CanonicalBatchProducedListener` subscribes to BC8's event via `@EventListener(EventEnvelope<CanonicalBatchProduced>)`
- Publishes `MappingRunStarted`, `MappingResultBatchProduced`, `MappingRunFailed` via `InProcessEventBus`
- Events use `EventEnvelope.of()` factory with class name as event type

## Idempotency
- Deterministic run ID from SHA-256 hash (same as BC8 batch ID pattern)
- `IdempotencyGuard` short-circuits on PRODUCED, throws on RUNNING (concurrent race detection)
- Override-set versioning (SHA-256 of sorted active override IDs) enables re-attribution on override changes

## Confidence Model
- `ConfidenceScorer` returns `(ConfidenceBand, score)` pairs
- Resolution reason codes: `MANUAL_OVERRIDE`, `EXPLICIT_BINDING`, `NO_MATCH`
- Score stored as `NUMERIC(4,3)` in DB (range 0.000–1.000)

## Cross-BC Adapters
- BC8 adapter: `CanonicalFactQueryAdapter` in normalisation module, implements BC9's `CanonicalFactQueryPort`
- BC5 adapter: `AccountBindingQueryAdapter` in googleadsmanagement module, implements BC9's `AccountBindingQueryPort`
- Wired explicitly in `ModuleRegistry` to make cross-BC dependencies visible

## REST API
- Controllers use `ApiResponse<T>` wrapper (consistent with all other modules)
- Tenant ID from `X-Tenant-Id` header or `tenantId` request attribute
- Pagination via `page`/`size` query params

## Testing
- Domain tests: pure unit, no mocks
- Application tests: `@ExtendWith(MockitoExtension.class)`, mock all ports
- Controller tests: `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters = false)` + `@MockitoBean`
