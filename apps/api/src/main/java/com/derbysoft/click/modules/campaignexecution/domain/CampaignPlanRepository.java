package com.derbysoft.click.modules.campaignexecution.domain;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignPlanRepository {
    Optional<CampaignPlan> findById(UUID id);
    List<CampaignPlan> findByTenantId(UUID tenantId);
    CampaignPlan save(CampaignPlan plan);
}
