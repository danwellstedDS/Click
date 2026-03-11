# BC8 Test Suite — Shape

## Test Directory Structure

```
apps/api/src/test/java/com/derbysoft/click/modules/normalisation/
├── domain/
│   ├── CanonicalBatchTest.java          (9 tests)
│   └── valueobjects/
│       ├── ReconciliationKeyTest.java   (4 tests)
│       └── MappingVersionTest.java      (2 tests)
└── application/
    ├── QualityValidatorTest.java        (9 tests)
    ├── BatchAssemblerTest.java          (4 tests)
    ├── NormalizerTest.java              (5 tests)
    ├── IdempotencyGuardTest.java        (4 tests)
    └── NormalisationServiceTest.java    (4 tests)
```

Total: 41 test cases across 8 classes.
