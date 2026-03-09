package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record PlanItemResponse(UUID id, UUID revisionId, String actionType, String resourceType,
                                String resourceId, String status, int attempts,
                                String failureClass, String failureReason,
                                Instant createdAt, Instant updatedAt,
                                String nextAction, String actionability) {}
