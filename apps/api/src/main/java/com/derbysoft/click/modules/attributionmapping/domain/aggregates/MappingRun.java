package com.derbysoft.click.modules.attributionmapping.domain.aggregates;

import com.derbysoft.click.modules.attributionmapping.domain.events.LowConfidenceMappingDetected;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingResultBatchProduced;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingRunFailed;
import com.derbysoft.click.modules.attributionmapping.domain.events.MappingRunStarted;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.RunStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class MappingRun {

    private final UUID id;
    private final UUID canonicalBatchId;
    private final UUID tenantId;
    private final String ruleSetVersion;
    private final String overrideSetVersion;
    private RunStatus status;
    private int mappedCount;
    private int lowConfidenceCount;
    private int unresolvedCount;
    private Instant startedAt;
    private Instant completedAt;
    private Instant failedAt;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private MappingRun(
        UUID id, UUID canonicalBatchId, UUID tenantId,
        String ruleSetVersion, String overrideSetVersion, RunStatus status,
        int mappedCount, int lowConfidenceCount, int unresolvedCount,
        Instant startedAt, Instant completedAt, Instant failedAt, String failureReason,
        Instant createdAt, Instant updatedAt
    ) {
        this.id = id;
        this.canonicalBatchId = canonicalBatchId;
        this.tenantId = tenantId;
        this.ruleSetVersion = ruleSetVersion;
        this.overrideSetVersion = overrideSetVersion;
        this.status = status;
        this.mappedCount = mappedCount;
        this.lowConfidenceCount = lowConfidenceCount;
        this.unresolvedCount = unresolvedCount;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MappingRun create(
        UUID canonicalBatchId, UUID tenantId,
        String ruleSetVersion, String overrideSetVersion, Instant now
    ) {
        UUID id = deterministicId(canonicalBatchId, ruleSetVersion, overrideSetVersion);
        MappingRun run = new MappingRun(
            id, canonicalBatchId, tenantId, ruleSetVersion, overrideSetVersion,
            RunStatus.RUNNING, 0, 0, 0, now, null, null, null, now, now
        );
        run.events.add(new MappingRunStarted(id, canonicalBatchId, tenantId, ruleSetVersion, overrideSetVersion, now));
        return run;
    }

    public static MappingRun reconstitute(
        UUID id, UUID canonicalBatchId, UUID tenantId,
        String ruleSetVersion, String overrideSetVersion, RunStatus status,
        int mappedCount, int lowConfidenceCount, int unresolvedCount,
        Instant startedAt, Instant completedAt, Instant failedAt, String failureReason,
        Instant createdAt, Instant updatedAt
    ) {
        return new MappingRun(
            id, canonicalBatchId, tenantId, ruleSetVersion, overrideSetVersion, status,
            mappedCount, lowConfidenceCount, unresolvedCount,
            startedAt, completedAt, failedAt, failureReason, createdAt, updatedAt
        );
    }

    public void produce(int mappedCount, int lowConfidenceCount, int unresolvedCount,
                        List<UUID> lowConfidenceFactIds, Instant now) {
        if (this.status == RunStatus.PRODUCED) {
            throw new IllegalStateException("MappingRun " + id + " is already PRODUCED and immutable");
        }
        this.status = RunStatus.PRODUCED;
        this.mappedCount = mappedCount;
        this.lowConfidenceCount = lowConfidenceCount;
        this.unresolvedCount = unresolvedCount;
        this.completedAt = now;
        this.updatedAt = now;
        events.add(new MappingResultBatchProduced(
            id, canonicalBatchId, tenantId, ruleSetVersion, overrideSetVersion,
            mappedCount, lowConfidenceCount, unresolvedCount, now
        ));
        if (lowConfidenceCount > 0 || unresolvedCount > 0) {
            events.add(new LowConfidenceMappingDetected(
                id, canonicalBatchId, tenantId,
                lowConfidenceCount, unresolvedCount,
                List.copyOf(lowConfidenceFactIds), now
            ));
        }
    }

    public void fail(String reason, Instant now) {
        this.status = RunStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = now;
        this.updatedAt = now;
        events.add(new MappingRunFailed(id, canonicalBatchId, tenantId, reason, now));
    }

    private static UUID deterministicId(UUID canonicalBatchId, String ruleSetVersion, String overrideSetVersion) {
        String seed = canonicalBatchId + ":" + ruleSetVersion + ":" + overrideSetVersion;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] uuidBytes = new byte[16];
            System.arraycopy(hash, 0, uuidBytes, 0, 16);
            return UUID.nameUUIDFromBytes(uuidBytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public UUID getId() { return id; }
    public UUID getCanonicalBatchId() { return canonicalBatchId; }
    public UUID getTenantId() { return tenantId; }
    public String getRuleSetVersion() { return ruleSetVersion; }
    public String getOverrideSetVersion() { return overrideSetVersion; }
    public RunStatus getStatus() { return status; }
    public int getMappedCount() { return mappedCount; }
    public int getLowConfidenceCount() { return lowConfidenceCount; }
    public int getUnresolvedCount() { return unresolvedCount; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getFailedAt() { return failedAt; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
