package com.derbysoft.click.modules.campaignexecution.domain.aggregates;

import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentAutoClosed;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentEscalated;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentOpened;
import com.derbysoft.click.modules.campaignexecution.domain.events.ExecutionIncidentReopened;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.IncidentStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ExecutionIncident {

    private static final Duration RECURRENCE_WINDOW = Duration.ofHours(24);

    private final UUID id;
    private final String idempotencyKey;
    private final UUID tenantId;
    private final FailureClass failureClass;
    private final UUID revisionId;
    private final UUID itemId;
    private final String failureClassKey;
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

    private ExecutionIncident(UUID id, String idempotencyKey, UUID tenantId,
                               FailureClass failureClass,
                               UUID revisionId, UUID itemId, String failureClassKey,
                               IncidentStatus status,
                               int consecutiveFailures,
                               Instant firstFailedAt, Instant lastFailedAt,
                               String acknowledgedBy, String ackReason, Instant acknowledgedAt,
                               Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.tenantId = tenantId;
        this.failureClass = failureClass;
        this.revisionId = revisionId;
        this.itemId = itemId;
        this.failureClassKey = failureClassKey;
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

    public static ExecutionIncident open(UUID id, UUID revisionId, UUID itemId,
                                          String failureClassKey, UUID tenantId,
                                          FailureClass failureClass, Instant now) {
        String derivedKey = revisionId + "/" + itemId + "/" + failureClassKey;
        ExecutionIncident incident = new ExecutionIncident(
            id, derivedKey, tenantId, failureClass,
            revisionId, itemId, failureClassKey,
            IncidentStatus.OPEN, 1,
            now, now,
            null, null, null,
            now, now
        );
        incident.events.add(new ExecutionIncidentOpened(id, derivedKey, tenantId, failureClass, now));
        return incident;
    }

    public static ExecutionIncident reconstitute(UUID id, String idempotencyKey, UUID tenantId,
                                                  FailureClass failureClass, IncidentStatus status,
                                                  int consecutiveFailures,
                                                  Instant firstFailedAt, Instant lastFailedAt,
                                                  UUID revisionId, UUID itemId, String failureClassKey,
                                                  String acknowledgedBy, String ackReason,
                                                  Instant acknowledgedAt,
                                                  Instant createdAt, Instant updatedAt) {
        return new ExecutionIncident(id, idempotencyKey, tenantId, failureClass,
            revisionId, itemId, failureClassKey,
            status, consecutiveFailures, firstFailedAt, lastFailedAt,
            acknowledgedBy, ackReason, acknowledgedAt,
            createdAt, updatedAt);
    }

    public void recordFailure(Instant now) {
        this.consecutiveFailures++;
        this.lastFailedAt = now;
        this.updatedAt = now;

        if (status == IncidentStatus.AUTO_CLOSED) {
            this.status = IncidentStatus.REOPENED;
            events.add(new ExecutionIncidentReopened(id, idempotencyKey, tenantId, now));
        }

        if (consecutiveFailures >= 3 && status != IncidentStatus.ESCALATED) {
            this.status = IncidentStatus.ESCALATED;
            events.add(new ExecutionIncidentEscalated(id, idempotencyKey, tenantId, consecutiveFailures, now));
        }
    }

    public void autoClose(Instant now) {
        this.status = IncidentStatus.AUTO_CLOSED;
        this.updatedAt = now;
        events.add(new ExecutionIncidentAutoClosed(id, idempotencyKey, tenantId, now));
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

    /**
     * Returns true if this AUTO_CLOSED incident's recurrence window (24h) has elapsed,
     * meaning a new failure should open a fresh incident rather than reopen this one.
     */
    public boolean isRecurrenceWindowExpired(Instant now) {
        if (status != IncidentStatus.AUTO_CLOSED) return false;
        return Duration.between(updatedAt, now).compareTo(RECURRENCE_WINDOW) >= 0;
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getTenantId() { return tenantId; }
    public FailureClass getFailureClass() { return failureClass; }
    public UUID getRevisionId() { return revisionId; }
    public UUID getItemId() { return itemId; }
    public String getFailureClassKey() { return failureClassKey; }
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
