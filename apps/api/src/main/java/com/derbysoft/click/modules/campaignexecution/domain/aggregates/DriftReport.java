package com.derbysoft.click.modules.campaignexecution.domain.aggregates;

import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.DriftSeverity;
import java.time.Instant;
import java.util.UUID;

public final class DriftReport {

    private final UUID id;
    private final UUID planId;
    private final UUID revisionId;
    private final UUID tenantId;
    private final DriftSeverity severity;
    private final String resourceType;
    private final String resourceId;
    private final String field;
    private final String intendedValue;
    private final String providerValue;
    private final Instant detectedAt;

    private DriftReport(UUID id, UUID planId, UUID revisionId, UUID tenantId,
                        DriftSeverity severity, String resourceType, String resourceId,
                        String field, String intendedValue, String providerValue,
                        Instant detectedAt) {
        this.id = id;
        this.planId = planId;
        this.revisionId = revisionId;
        this.tenantId = tenantId;
        this.severity = severity;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.field = field;
        this.intendedValue = intendedValue;
        this.providerValue = providerValue;
        this.detectedAt = detectedAt;
    }

    public static DriftReport create(UUID id, UUID planId, UUID revisionId, UUID tenantId,
                                      DriftSeverity severity, String resourceType, String resourceId,
                                      String field, String intendedValue, String providerValue,
                                      Instant detectedAt) {
        return new DriftReport(id, planId, revisionId, tenantId, severity,
            resourceType, resourceId, field, intendedValue, providerValue, detectedAt);
    }

    public UUID getId() { return id; }
    public UUID getPlanId() { return planId; }
    public UUID getRevisionId() { return revisionId; }
    public UUID getTenantId() { return tenantId; }
    public DriftSeverity getSeverity() { return severity; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getField() { return field; }
    public String getIntendedValue() { return intendedValue; }
    public String getProviderValue() { return providerValue; }
    public Instant getDetectedAt() { return detectedAt; }
}
