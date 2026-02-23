package api.application.dto;

import java.time.Instant;
import java.util.UUID;

public record UserListItemResponse(
    UUID id,
    String email,
    String role,
    Instant createdAt
) {}
