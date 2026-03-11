# BC8 — Normalisation (Metrics Canon) — Implementation Plan

**Date**: 2026-03-09
**Status**: MVP-1 Implemented
**Branch**: feature/bc7-mock-data-seed

## Scope — MVP-1

Google Ads campaign-day normalization:
- Metrics: `impressions`, `clicks`, `costMicros`, `conversions`
- Grain: per campaign per day
- Triggered by: `RawSnapshotWritten` events from BC7
- Output: `CanonicalBatchProduced` for BC9

## Module Structure (implemented)

```
normalisation/
├── api/
│   ├── contracts/         CanonicalBatchInfo, CanonicalFactInfo, NormalisationQualityStats
│   └── ports/             NormalisationQueryPort
├── application/
│   ├── handlers/          NormalisationService, RawSnapshotWrittenListener
│   ├── ports/             RawCampaignRowQueryPort
│   └── services/          Normalizer, BatchAssembler, QualityValidator, IdempotencyGuard
├── domain/
│   ├── aggregates/        CanonicalBatch
│   ├── events/            5 domain events
│   ├── valueobjects/      BatchStatus, MappingVersion, ReconciliationKey, QualityFlag
│   └── *Repository.java   CanonicalBatchRepository, CanonicalFactRepository
└── infrastructure/
    └── persistence/
        ├── entity/        CanonicalBatchEntity, CanonicalFactEntity
        ├── mapper/        CanonicalBatchMapper
        └── repository/    CanonicalBatchRepositoryImpl (dual-interface), CanonicalFactRepositoryImpl,
                           CanonicalBatchJpaRepository, CanonicalFactJpaRepository
└── interfaces/
    └── http/
        ├── controller/    CanonicalBatchController
        └── dto/           CanonicalBatchResponse, CanonicalFactResponse, NormalizationQualityReport
```

BC7 adapter: `ingestion/infrastructure/persistence/repository/RawCampaignRowQueryAdapter.java`

## Migrations

- `V202603090003__create_canonical_batches.sql`
- `V202603090004__create_canonical_facts.sql`

## Key Design Decisions

1. **Deterministic batch ID**: `UUID.nameUUIDFromBytes(SHA-256(snapshotId + ":" + mappingVersion))` — ensures idempotent batch identity
2. **Dual-interface pattern**: `CanonicalBatchRepositoryImpl` implements both `CanonicalBatchRepository` (domain) and `NormalisationQueryPort` (public API) — wired in `ModuleRegistry`
3. **Quality flags as TEXT[]**: PostgreSQL native array; mapped via Hibernate 6 `@JdbcTypeCode(SqlTypes.ARRAY)`
4. **Idempotency**: `IdempotencyGuard` short-circuits if batch already PRODUCED; throws on concurrent PROCESSING
5. **Channel hardcoded**: `GOOGLE_ADS` — future channels extend `Normalizer` dispatch logic
