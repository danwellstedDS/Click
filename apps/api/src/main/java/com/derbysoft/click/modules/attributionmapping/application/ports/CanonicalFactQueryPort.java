package com.derbysoft.click.modules.attributionmapping.application.ports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CanonicalFactQueryPort {

    List<CanonicalFactData> findByBatchId(UUID batchId);

    record CanonicalFactData(
        UUID id,
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
        BigDecimal costAmount,
        BigDecimal conversions,
        boolean quarantined
    ) {}
}
