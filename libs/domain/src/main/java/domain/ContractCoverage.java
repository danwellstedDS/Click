package domain;

import java.time.Instant;
import java.util.UUID;

public record ContractCoverage(
    UUID id,
    UUID contractId,
    UUID scopeId,
    Instant createdAt
) {}
