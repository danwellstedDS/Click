package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort;
import java.util.List;
import java.util.UUID;

/**
 * BC7 adapter implementing BC8's {@link RawCampaignRowQueryPort}.
 * Reads raw campaign rows from BC7's persistence layer.
 */
public class RawCampaignRowQueryAdapter implements RawCampaignRowQueryPort {

    private final RawCampaignRowJpaRepository rawCampaignRowJpaRepository;

    public RawCampaignRowQueryAdapter(RawCampaignRowJpaRepository rawCampaignRowJpaRepository) {
        this.rawCampaignRowJpaRepository = rawCampaignRowJpaRepository;
    }

    @Override
    public List<RawCampaignRowData> findBySnapshotId(UUID snapshotId) {
        return rawCampaignRowJpaRepository.findBySnapshotId(snapshotId).stream()
            .map(e -> new RawCampaignRowData(
                e.getId(), e.getSnapshotId(), e.getIntegrationId(), e.getAccountId(),
                e.getCampaignId(), e.getCampaignName(), e.getReportDate(),
                e.getClicks(), e.getImpressions(), e.getCostMicros(), e.getConversions()
            ))
            .toList();
    }
}
