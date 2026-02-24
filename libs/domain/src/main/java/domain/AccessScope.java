package domain;

import java.time.Instant;
import java.util.UUID;

public record AccessScope(
    UUID id,
    ScopeType type,
    UUID chainId,
    UUID hotelId,
    UUID portfolioId,
    Instant createdAt
) {}
