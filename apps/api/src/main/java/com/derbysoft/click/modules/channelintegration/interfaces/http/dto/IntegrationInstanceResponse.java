package com.derbysoft.click.modules.channelintegration.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record IntegrationInstanceResponse(
    UUID id,
    UUID tenantId,
    String channel,
    String connectionKey,
    String status,
    UUID credentialId,
    String cadenceType,
    String cronExpression,
    Integer intervalMinutes,
    String timezone,
    Instant lastSyncAt,
    String lastSyncStatus,
    Instant lastSuccessAt,
    String lastErrorCode,
    String lastErrorMessage,
    int consecutiveFailures,
    String statusReason,
    Instant credentialAttachedAt,
    Instant createdAt,
    Instant updatedAt
) {}
