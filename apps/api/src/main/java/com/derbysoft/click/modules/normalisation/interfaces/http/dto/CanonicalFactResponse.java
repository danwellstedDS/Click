package com.derbysoft.click.modules.normalisation.interfaces.http.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CanonicalFactResponse(
    UUID id,
    UUID canonicalBatchId,
    UUID sourceSnapshotId,
    UUID tenantId,
    String channel,
    UUID integrationId,
    String customerAccountId,
    String campaignId,
    String campaignName,
    LocalDate reportDate,
    long impressions,
    long clicks,
    long costMicros,
    BigDecimal conversions,
    String mappingVersion,
    String reconciliationKey,
    List<String> qualityFlags,
    boolean quarantined,
    Instant ingestedAt
) {}
