# BC8 Test Suite — Plan

## Goal
Add full test coverage for the BC8 normalisation module (canonical batch production from raw Google Ads snapshots).

## Scope
8 new test classes covering domain aggregates, value objects, and application services.

## Test Files

| File | Layer | Coverage |
|---|---|---|
| `domain/CanonicalBatchTest.java` | Domain | Aggregate state machine, event emission, immutability |
| `domain/valueobjects/ReconciliationKeyTest.java` | Domain | SHA-256 key determinism and uniqueness |
| `domain/valueobjects/MappingVersionTest.java` | Domain | Blank-value validation, V1 constant |
| `application/QualityValidatorTest.java` | Application | All 5 quality flags, multi-flag, null conversions |
| `application/BatchAssemblerTest.java` | Application | Checksum determinism and order-independence |
| `application/NormalizerTest.java` | Application | Raw → canonical field mapping, key assignment |
| `application/IdempotencyGuardTest.java` | Application | Empty→proceed, PRODUCED→skip, PROCESSING→throw |
| `application/NormalisationServiceTest.java` | Application | Full pipeline: happy path, quarantine, idempotency replay, failure |

## Standards
- JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- AssertJ assertions
- No mocks in domain tests
- Mocked ports/repos in application tests
