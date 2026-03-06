package com.derbysoft.click.modules.campaignexecution.domain;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.DriftReport;
import java.util.List;
import java.util.UUID;

public interface DriftReportRepository {
    List<DriftReport> findByPlanId(UUID planId);
    List<DriftReport> findByRevisionId(UUID revisionId);
    DriftReport save(DriftReport report);
}
