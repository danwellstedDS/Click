package domain;

import java.time.Instant;
import java.util.UUID;

public record AccessScope(
    UUID id,
    ScopeType type,
    UUID propertyGroupId,
    UUID propertyId,
    UUID portfolioId,
    Instant createdAt
) {}
