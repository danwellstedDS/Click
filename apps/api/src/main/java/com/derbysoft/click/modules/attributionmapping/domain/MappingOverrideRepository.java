package com.derbysoft.click.modules.attributionmapping.domain;

import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingOverride;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MappingOverrideRepository {
    MappingOverride save(MappingOverride override);
    Optional<MappingOverride> findById(UUID id);
    List<MappingOverrideEntity> findActiveByTenantId(UUID tenantId);
    Optional<MappingOverrideEntity> findMatchingOverride(UUID tenantId, String customerAccountId, String campaignId);
}
