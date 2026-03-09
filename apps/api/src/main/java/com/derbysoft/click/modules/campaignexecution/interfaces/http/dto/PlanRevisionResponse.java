package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record PlanRevisionResponse(UUID id, UUID planId, UUID tenantId, int revisionNumber,
                                    String status, String publishedBy, Instant publishedAt,
                                    Instant createdAt, Instant updatedAt) {}
