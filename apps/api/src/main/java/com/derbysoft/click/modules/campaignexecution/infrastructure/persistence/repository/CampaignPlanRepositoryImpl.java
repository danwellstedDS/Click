package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.domain.CampaignPlanRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.CampaignPlanMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CampaignPlanRepositoryImpl implements CampaignPlanRepository {

    private final CampaignPlanJpaRepository jpaRepository;
    private final CampaignPlanMapper mapper;

    public CampaignPlanRepositoryImpl(CampaignPlanJpaRepository jpaRepository,
                                       CampaignPlanMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CampaignPlan> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<CampaignPlan> findByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public CampaignPlan save(CampaignPlan plan) {
        var entity = mapper.toEntity(plan);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
