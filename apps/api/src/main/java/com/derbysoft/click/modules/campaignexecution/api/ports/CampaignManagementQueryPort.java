package com.derbysoft.click.modules.campaignexecution.api.ports;

import com.derbysoft.click.modules.campaignexecution.api.contracts.ExecutionIncidentSummary;
import com.derbysoft.click.modules.campaignexecution.api.contracts.PlanItemInfo;
import com.derbysoft.click.modules.campaignexecution.api.contracts.PlanRevisionInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignManagementQueryPort {
    List<PlanRevisionInfo> listAppliedRevisions(UUID tenantId);
    Optional<PlanRevisionInfo> findRevisionById(UUID revisionId);
    List<PlanItemInfo> listSucceededItems(UUID revisionId);
    List<ExecutionIncidentSummary> listOpenIncidents(UUID tenantId);
    List<ExecutionIncidentSummary> listEscalatedIncidents(UUID tenantId);
}
