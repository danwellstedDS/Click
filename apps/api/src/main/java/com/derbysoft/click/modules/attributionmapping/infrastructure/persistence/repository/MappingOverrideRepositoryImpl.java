package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.domain.MappingOverrideRepository;
import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingOverride;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.mapper.MappingOverrideMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MappingOverrideRepositoryImpl implements MappingOverrideRepository {

    private final MappingOverrideJpaRepository jpaRepository;
    private final MappingOverrideMapper mapper;

    public MappingOverrideRepositoryImpl(
        MappingOverrideJpaRepository jpaRepository,
        MappingOverrideMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MappingOverride save(MappingOverride override) {
        MappingOverrideEntity entity = mapper.toEntity(override);
        MappingOverrideEntity saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MappingOverride> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MappingOverrideEntity> findActiveByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantIdAndStatus(tenantId, "ACTIVE");
    }

    @Override
    public Optional<MappingOverrideEntity> findMatchingOverride(
        UUID tenantId, String customerAccountId, String campaignId
    ) {
        if (campaignId != null) {
            Optional<MappingOverrideEntity> exact = jpaRepository
                .findByTenantIdAndCustomerAccountIdAndStatusAndCampaignId(
                    tenantId, customerAccountId, "ACTIVE", campaignId);
            if (exact.isPresent()) return exact;
        }
        return jpaRepository.findByTenantIdAndCustomerAccountIdAndStatusAndCampaignIdIsNull(
            tenantId, customerAccountId, "ACTIVE");
    }
}
