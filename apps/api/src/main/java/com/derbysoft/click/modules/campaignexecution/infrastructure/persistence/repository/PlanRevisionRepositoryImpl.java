package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.PlanRevisionMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlanRevisionRepositoryImpl implements PlanRevisionRepository {

    private final PlanRevisionJpaRepository jpaRepository;
    private final PlanRevisionMapper mapper;

    public PlanRevisionRepositoryImpl(PlanRevisionJpaRepository jpaRepository,
                                       PlanRevisionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PlanRevision> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PlanRevision> findByPlanId(UUID planId) {
        return jpaRepository.findByPlanId(planId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<PlanRevision> findActiveApplyingByTenantId(UUID tenantId) {
        return jpaRepository.findFirstByTenantIdAndStatus(tenantId, "APPLYING")
            .map(mapper::toDomain);
    }

    @Override
    public PlanRevision save(PlanRevision revision) {
        var entity = mapper.toEntity(revision);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
