package com.derbysoft.click.modules.ingestion.domain.aggregates;

import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentAutoClosed;
import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentEscalated;
import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentOpened;
import com.derbysoft.click.modules.ingestion.domain.events.SyncIncidentReopened;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.IncidentStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class SyncIncident {

    private final UUID id;
    private final String idempotencyKey;
    private final UUID tenantId;
    private final FailureClass failureClass;
    private IncidentStatus status;
    private int consecutiveFailures;
    private final Instant firstFailedAt;
    private Instant lastFailedAt;
    private String acknowledgedBy;
    private String ackReason;
    private Instant acknowledgedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private SyncIncident(
        UUID id, String idempotencyKey, UUID tenantId, FailureClass failureClass,
        IncidentStatus status, int consecutiveFailures,
        Instant firstFailedAt, Instant lastFailedAt,
        String acknowledgedBy, String ackReason, Instant acknowledgedAt,
        Instant createdAt, Instant updatedAt
    ) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.tenantId = tenantId;
        this.failureClass = failureClass;
        this.status = status;
        this.consecutiveFailures = consecutiveFailures;
        this.firstFailedAt = firstFailedAt;
        this.lastFailedAt = lastFailedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.ackReason = ackReason;
        this.acknowledgedAt = acknowledgedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SyncIncident open(UUID id, String idempotencyKey, UUID tenantId,
                                     FailureClass failureClass, Instant now) {
        SyncIncident incident = new SyncIncident(
            id, idempotencyKey, tenantId, failureClass,
            IncidentStatus.OPEN, 1,
            now, now,
            null, null, null,
            now, now
        );
        incident.events.add(new SyncIncidentOpened(id, idempotencyKey, tenantId, failureClass, now));
        return incident;
    }

    public static SyncIncident reconstitute(
        UUID id, String idempotencyKey, UUID tenantId, FailureClass failureClass,
        IncidentStatus status, int consecutiveFailures,
        Instant firstFailedAt, Instant lastFailedAt,
        String acknowledgedBy, String ackReason, Instant acknowledgedAt,
        Instant createdAt, Instant updatedAt
    ) {
        return new SyncIncident(
            id, idempotencyKey, tenantId, failureClass,
            status, consecutiveFailures,
            firstFailedAt, lastFailedAt,
            acknowledgedBy, ackReason, acknowledgedAt,
            createdAt, updatedAt
        );
    }

    public void recordFailure(Instant now) {
        this.consecutiveFailures++;
        this.lastFailedAt = now;
        this.updatedAt = now;

        if (status == IncidentStatus.AUTO_CLOSED) {
            this.status = IncidentStatus.REOPENED;
            events.add(new SyncIncidentReopened(id, idempotencyKey, tenantId, now));
        }

        if (consecutiveFailures >= 3 && status != IncidentStatus.ESCALATED) {
            this.status = IncidentStatus.ESCALATED;
            events.add(new SyncIncidentEscalated(id, idempotencyKey, tenantId, consecutiveFailures, now));
        }
    }

    public void autoClose(Instant now) {
        this.status = IncidentStatus.AUTO_CLOSED;
        this.updatedAt = now;
        events.add(new SyncIncidentAutoClosed(id, idempotencyKey, tenantId, now));
    }

    public void acknowledge(String ackReason, String by, Instant now) {
        if (status != IncidentStatus.ESCALATED) {
            throw new DomainError.Conflict("INC_409",
                "Can only acknowledge ESCALATED incidents; current status: " + status);
        }
        this.acknowledgedBy = by;
        this.ackReason = ackReason;
        this.acknowledgedAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getTenantId() { return tenantId; }
    public FailureClass getFailureClass() { return failureClass; }
    public IncidentStatus getStatus() { return status; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public Instant getFirstFailedAt() { return firstFailedAt; }
    public Instant getLastFailedAt() { return lastFailedAt; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public String getAckReason() { return ackReason; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
