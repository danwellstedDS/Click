package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record ExecutionSummaryUpdated(UUID revisionId, UUID tenantId,
                                       int queued, int inProgress, int succeeded,
                                       int failed, int blocked, Instant occurredAt) {}
