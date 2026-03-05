package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record ConnectionResponse(
    UUID id,
    UUID tenantId,
    String managerId,
    String status,
    Instant lastDiscoveredAt,
    Instant createdAt
) {}
