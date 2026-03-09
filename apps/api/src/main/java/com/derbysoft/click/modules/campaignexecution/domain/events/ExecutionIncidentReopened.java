package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ExecutionIncidentReopened(UUID incidentId, String idempotencyKey, UUID tenantId,
                                         Instant occurredAt) {}
