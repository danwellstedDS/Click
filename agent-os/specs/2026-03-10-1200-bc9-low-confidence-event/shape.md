# Shape: LowConfidenceMappingDetected

## Event Record

```java
public record LowConfidenceMappingDetected(
    UUID runId,
    UUID canonicalBatchId,
    UUID tenantId,
    int lowConfidenceCount,
    int unresolvedCount,
    List<UUID> lowConfidenceFactIds,
    Instant occurredAt
) {}
```

## Emission Rule

Emitted by `MappingRun.produce()` when `lowConfidenceCount > 0 || unresolvedCount > 0`,
after `MappingResultBatchProduced`.

`lowConfidenceFactIds` includes IDs for facts with `ConfidenceBand.LOW` or `ConfidenceBand.UNRESOLVED`.

## Invariants

- Zero or one per mapping run.
- Never emitted when all facts are HIGH confidence.
- `List.copyOf()` ensures immutability at event construction time.
