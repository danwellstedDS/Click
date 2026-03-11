package com.derbysoft.click.modules.normalisation.application.ports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RawCampaignRowQueryPort {
    List<RawCampaignRowData> findBySnapshotId(UUID snapshotId);

    record RawCampaignRowData(
        UUID id,
        UUID snapshotId,
        UUID integrationId,
        String accountId,
        String campaignId,
        String campaignName,
        LocalDate reportDate,
        long clicks,
        long impressions,
        long costMicros,
        BigDecimal conversions
    ) {}
}
