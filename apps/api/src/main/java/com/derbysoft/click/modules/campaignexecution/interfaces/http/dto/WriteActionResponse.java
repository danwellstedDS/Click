package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record WriteActionResponse(UUID id, UUID revisionId, UUID itemId, String actionType,
                                   String idempotencyKey, String status, int attempts,
                                   String triggerType, String failureClass, String failureReason,
                                   Instant createdAt, Instant updatedAt) {}
