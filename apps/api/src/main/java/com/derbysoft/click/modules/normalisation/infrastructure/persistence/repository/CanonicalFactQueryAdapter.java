package com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.application.ports.CanonicalFactQueryPort;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalFactEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * BC8 → BC9 adapter: exposes BC8's canonical facts to BC9's attribution pipeline
 * via {@link CanonicalFactQueryPort}.
 */
public class CanonicalFactQueryAdapter implements CanonicalFactQueryPort {

    private final CanonicalFactJpaRepository jpaRepository;

    public CanonicalFactQueryAdapter(CanonicalFactJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CanonicalFactData> findByBatchId(UUID batchId) {
        return jpaRepository.findByCanonicalBatchId(batchId, Pageable.unpaged())
            .stream()
            .map(this::toData)
            .toList();
    }

    private CanonicalFactData toData(CanonicalFactEntity e) {
        return new CanonicalFactData(
            e.getId(), e.getTenantId(), e.getChannel(), e.getIntegrationId(),
            e.getCustomerAccountId(), e.getCampaignId(), e.getCampaignName(),
            e.getReportDate(), e.getImpressions(), e.getClicks(), e.getCostMicros(),
            e.getCostAmount(), e.getConversions(), e.isQuarantined()
        );
    }
}
