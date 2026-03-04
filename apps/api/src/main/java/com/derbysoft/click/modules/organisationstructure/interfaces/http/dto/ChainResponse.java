package com.derbysoft.click.modules.organisationstructure.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record ChainResponse(
    UUID id,
    String name,
    String status,
    String timezone,
    String currency,
    UUID primaryOrgId,
    Instant createdAt,
    Instant updatedAt
) {}
