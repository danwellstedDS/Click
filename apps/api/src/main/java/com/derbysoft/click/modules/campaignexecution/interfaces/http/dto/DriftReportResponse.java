package com.derbysoft.click.modules.campaignexecution.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record DriftReportResponse(UUID id, UUID planId, UUID revisionId, String severity,
                                   String resourceType, String resourceId, String field,
                                   String intendedValue, String providerValue,
                                   Instant detectedAt) {}
