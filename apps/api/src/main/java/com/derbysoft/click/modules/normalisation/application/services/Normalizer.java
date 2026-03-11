package com.derbysoft.click.modules.normalisation.application.services;

import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort.RawCampaignRowData;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.ReconciliationKey;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class Normalizer {

    public CanonicalFactEntity map(RawCampaignRowData row, UUID batchId, MappingVersion mappingVersion, Instant now) {
        ReconciliationKey key = ReconciliationKey.from(
            row.integrationId(), row.accountId(), row.campaignId(), row.reportDate()
        );
        CanonicalFactEntity entity = new CanonicalFactEntity();
        entity.setId(UUID.randomUUID());
        entity.setCanonicalBatchId(batchId);
        entity.setSourceSnapshotId(row.snapshotId());
        entity.setTenantId(null); // set by caller
        entity.setChannel("GOOGLE_ADS");
        entity.setIntegrationId(row.integrationId());
        entity.setCustomerAccountId(row.accountId());
        entity.setCampaignId(row.campaignId());
        entity.setCampaignName(row.campaignName());
        entity.setReportDate(row.reportDate());
        entity.setImpressions(row.impressions());
        entity.setClicks(row.clicks());
        entity.setCostMicros(row.costMicros());
        entity.setConversions(row.conversions());
        entity.setMappingVersion(mappingVersion.value());
        entity.setReconciliationKey(key.value());
        entity.setQualityFlags(new String[0]);
        entity.setQuarantined(false);
        entity.setIngestedAt(now);
        return entity;
    }
}
