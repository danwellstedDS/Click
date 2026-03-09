package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.repository;

import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.DriftReportEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriftReportJpaRepository extends JpaRepository<DriftReportEntity, UUID> {
    List<DriftReportEntity> findByPlanId(UUID planId);
    List<DriftReportEntity> findByRevisionId(UUID revisionId);
}
