package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drift_reports")
public class DriftReportEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "plan_id", nullable = false, updatable = false)
    private UUID planId;

    @Column(name = "revision_id", nullable = false, updatable = false)
    private UUID revisionId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 10, updatable = false)
    private String severity;

    @Column(name = "resource_type", nullable = false, length = 30, updatable = false)
    private String resourceType;

    @Column(name = "resource_id", nullable = false, length = 255, updatable = false)
    private String resourceId;

    @Column(nullable = false, length = 255, updatable = false)
    private String field;

    @Column(name = "intended_value", columnDefinition = "TEXT", updatable = false)
    private String intendedValue;

    @Column(name = "provider_value", columnDefinition = "TEXT", updatable = false)
    private String providerValue;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    protected DriftReportEntity() {}

    public DriftReportEntity(UUID id, UUID planId, UUID revisionId, UUID tenantId,
                              String severity, String resourceType, String resourceId,
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

    public UUID getId() { return id; }
    public UUID getPlanId() { return planId; }
    public UUID getRevisionId() { return revisionId; }
    public UUID getTenantId() { return tenantId; }
    public String getSeverity() { return severity; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getField() { return field; }
    public String getIntendedValue() { return intendedValue; }
    public String getProviderValue() { return providerValue; }
    public Instant getDetectedAt() { return detectedAt; }
}
