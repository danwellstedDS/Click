package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.CampaignPlanEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignPlanJpaRepository extends JpaRepository<CampaignPlanEntity, UUID> {
    List<CampaignPlanEntity> findByTenantId(UUID tenantId);
}
