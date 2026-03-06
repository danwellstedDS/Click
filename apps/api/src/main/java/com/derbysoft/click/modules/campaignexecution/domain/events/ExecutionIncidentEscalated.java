package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ExecutionIncidentEscalated(UUID incidentId, String idempotencyKey, UUID tenantId,
                                          int consecutiveFailures, Instant occurredAt) {}
