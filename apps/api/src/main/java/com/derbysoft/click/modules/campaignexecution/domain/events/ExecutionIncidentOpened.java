package com.derbysoft.click.modules.campaignexecution.domain.events;

import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import java.time.Instant;
import java.util.UUID;

public record ExecutionIncidentOpened(UUID incidentId, String idempotencyKey, UUID tenantId,
                                       FailureClass failureClass, Instant occurredAt) {}
