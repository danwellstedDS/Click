package com.derbysoft.click.modules.campaignexecution.api.contracts;

import java.time.Instant;
import java.util.UUID;

public record ExecutionIncidentSummary(UUID incidentId, String idempotencyKey, UUID tenantId,
                                        String failureClass, String status,
                                        int consecutiveFailures, Instant lastFailedAt,
                                        String acknowledgedBy, String ackReason) {}
