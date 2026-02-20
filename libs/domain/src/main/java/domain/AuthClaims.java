package domain;

import java.util.UUID;

public record AuthClaims(UUID userId, UUID tenantId, String email, Role role) {}
