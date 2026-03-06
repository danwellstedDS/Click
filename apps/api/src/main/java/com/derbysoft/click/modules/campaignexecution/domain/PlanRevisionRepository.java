package com.derbysoft.click.modules.campaignexecution.domain;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRevisionRepository {
    Optional<PlanRevision> findById(UUID id);
    List<PlanRevision> findByPlanId(UUID planId);
    Optional<PlanRevision> findActiveApplyingByTenantId(UUID tenantId);
    PlanRevision save(PlanRevision revision);
}
