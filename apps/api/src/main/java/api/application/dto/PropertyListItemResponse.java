package api.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PropertyListItemResponse(
    UUID id,
    String name,
    boolean isActive,
    String externalPropertyRef,
    Instant createdAt
) {}
