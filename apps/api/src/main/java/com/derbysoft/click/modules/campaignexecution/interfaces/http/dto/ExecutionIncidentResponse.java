package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record ExecutionIncidentResponse(UUID id, String idempotencyKey, UUID tenantId,
                                         String failureClass, String status,
                                         int consecutiveFailures, Instant lastFailedAt,
                                         String acknowledgedBy, String ackReason,
                                         String nextAction, String actionability) {}
