package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.CampaignPlanEntity;
import org.springframework.stereotype.Component;

@Component
public class CampaignPlanMapper {

    public CampaignPlan toDomain(CampaignPlanEntity entity) {
        return CampaignPlan.reconstitute(
            entity.getId(),
            entity.getTenantId(),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public CampaignPlanEntity toEntity(CampaignPlan domain) {
        return new CampaignPlanEntity(
            domain.getId(),
            domain.getTenantId(),
            domain.getName(),
            domain.getDescription()
        );
    }
}
