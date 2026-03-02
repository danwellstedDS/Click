package com.derbysoft.click.modules.channelintegration.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record IntegrationInstanceResponse(
    UUID id,
    UUID tenantId,
    String channel,
    String status,
    UUID credentialId,
    String cron,
    String timezone,
    Instant createdAt,
    Instant updatedAt
) {}
