# BC8 Test Suite — References

## Source Files Tested

| Source | Package |
|---|---|
| `CanonicalBatch` | `normalisation.domain.aggregates` |
| `ReconciliationKey` | `normalisation.domain.valueobjects` |
| `MappingVersion` | `normalisation.domain.valueobjects` |
| `QualityValidator` | `normalisation.application.services` |
| `BatchAssembler` | `normalisation.application.services` |
| `Normalizer` | `normalisation.application.services` |
| `IdempotencyGuard` | `normalisation.application.services` |
| `NormalisationService` | `normalisation.application.handlers` |

## Reference Test Implementations
- `attributionmapping/application/AttributionServiceTest.java` — service test pattern with mocked repos
- `attributionmapping/domain/MappingRunTest.java` — domain aggregate test pattern
- `ingestion/domain/SyncJobTest.java` — domain event emission pattern
- `ingestion/application/IngestionJobServiceTest.java` — full pipeline service test pattern
