package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.domain.DriftReportRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.DriftReport;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper.DriftReportMapper;
import java.util.List;
import java.util.UUID;

public class DriftReportRepositoryImpl implements DriftReportRepository {

    private final DriftReportJpaRepository jpaRepository;
    private final DriftReportMapper mapper;

    public DriftReportRepositoryImpl(DriftReportJpaRepository jpaRepository,
                                      DriftReportMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<DriftReport> findByPlanId(UUID planId) {
        return jpaRepository.findByPlanId(planId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<DriftReport> findByRevisionId(UUID revisionId) {
        return jpaRepository.findByRevisionId(revisionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public DriftReport save(DriftReport report) {
        var entity = mapper.toEntity(report);
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }
}
