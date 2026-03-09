package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.modules.tenantgovernance.api.ports.TenantGovernancePort;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PublishValidationService {

    private final GoogleAdsQueryPort googleAdsQueryPort;
    private final TenantGovernancePort governancePort;

    public PublishValidationService(GoogleAdsQueryPort googleAdsQueryPort,
                                     TenantGovernancePort governancePort) {
        this.googleAdsQueryPort = googleAdsQueryPort;
        this.governancePort = governancePort;
    }

    public void validate(PlanRevision revision, List<PlanItem> items, UUID tenantId) {
        // Gap #1: governance gate before publish
        governancePort.assertCanExecuteCampaigns(tenantId);

        var connection = googleAdsQueryPort.findConnectionByTenantId(tenantId)
            .orElseThrow(() -> new DomainError.ValidationError("CE_400",
                "No Google Ads connection found for tenant: " + tenantId));

        if (!"ACTIVE".equals(connection.status())) {
            throw new DomainError.ValidationError("CE_400",
                "Google Ads connection is not ACTIVE for tenant: " + tenantId);
        }

        var bindings = googleAdsQueryPort.listActiveBindings(tenantId);
        if (bindings.isEmpty()) {
            throw new DomainError.ValidationError("CE_400",
                "No active account bindings found for tenant: " + tenantId);
        }

        if (items.isEmpty()) {
            throw new DomainError.ValidationError("CE_400",
                "Revision must contain at least one item before publishing");
        }

        for (PlanItem item : items) {
            if (item.getPayload() == null || item.getPayload().isBlank()) {
                throw new DomainError.ValidationError("CE_400",
                    "Item " + item.getId() + " has empty payload");
            }
            validateJson(item.getId().toString(), item.getPayload());
        }
    }

    private void validateJson(String itemId, String payload) {
        String trimmed = payload.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new DomainError.ValidationError("CE_400",
                "Item " + itemId + " payload is not valid JSON");
        }
    }
}
