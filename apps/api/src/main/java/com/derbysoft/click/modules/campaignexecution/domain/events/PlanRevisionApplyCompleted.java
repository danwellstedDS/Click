package com.derbysoft.click.modules.campaignexecution.domain.events;

import java.time.Instant;
import java.util.UUID;

public record PlanRevisionApplyCompleted(UUID revisionId, UUID planId, UUID tenantId,
                                          int succeededCount, int failedCount,
                                          Instant occurredAt) {}
