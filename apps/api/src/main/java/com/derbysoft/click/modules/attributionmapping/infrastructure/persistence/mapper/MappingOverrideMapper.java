package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingOverride;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.OverrideScope;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import org.springframework.stereotype.Component;

@Component
public class MappingOverrideMapper {

    public MappingOverrideEntity toEntity(MappingOverride override) {
        MappingOverrideEntity entity = new MappingOverrideEntity();
        entity.setId(override.getId());
        entity.setTenantId(override.getTenantId());
        entity.setScopeType(override.getScopeType().name());
        entity.setCustomerAccountId(override.getCustomerAccountId());
        entity.setCampaignId(override.getCampaignId());
        entity.setTargetOrgNodeId(override.getTargetOrgNodeId());
        entity.setTargetScopeType(override.getTargetScopeType());
        entity.setReason(override.getReason());
        entity.setActor(override.getActor());
        entity.setStatus(override.getStatus());
        entity.setRemovedAt(override.getRemovedAt());
        entity.setRemovedReason(override.getRemovedReason());
        entity.setCreatedAt(override.getCreatedAt());
        entity.setUpdatedAt(override.getUpdatedAt());
        return entity;
    }

    public MappingOverride toDomain(MappingOverrideEntity entity) {
        return MappingOverride.reconstitute(
            entity.getId(), entity.getTenantId(),
            OverrideScope.valueOf(entity.getScopeType()),
            entity.getCustomerAccountId(), entity.getCampaignId(),
            entity.getTargetOrgNodeId(), entity.getTargetScopeType(),
            entity.getReason(), entity.getActor(), entity.getStatus(),
            entity.getRemovedAt(), entity.getRemovedReason(),
            entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
