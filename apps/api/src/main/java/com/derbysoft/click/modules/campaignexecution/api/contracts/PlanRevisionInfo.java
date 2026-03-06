package com.derbysoft.click.modules.campaignexecution.api.contracts;

import java.time.Instant;
import java.util.UUID;

public record PlanRevisionInfo(UUID revisionId, UUID planId, UUID tenantId,
                                int revisionNumber, String status,
                                String publishedBy, Instant publishedAt,
                                Instant createdAt) {}
