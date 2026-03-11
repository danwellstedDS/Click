package com.derbysoft.click.modules.normalisation.domain.aggregates;

import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchFailed;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchProduced;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchRebuilt;
import com.derbysoft.click.modules.normalisation.domain.events.CanonicalBatchStarted;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.BatchStatus;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CanonicalBatch {

    private final UUID id;
    private final UUID sourceSnapshotId;
    private final UUID integrationId;
    private final UUID tenantId;
    private final String accountId;
    private final MappingVersion mappingVersion;
    private BatchStatus status;
    private int factCount;
    private int quarantinedCount;
    private String checksum;
    private Instant producedAt;
    private Instant failedAt;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private CanonicalBatch(
        UUID id, UUID sourceSnapshotId, UUID integrationId, UUID tenantId,
        String accountId, MappingVersion mappingVersion, BatchStatus status,
        int factCount, int quarantinedCount, String checksum,
        Instant producedAt, Instant failedAt, String failureReason,
        Instant createdAt, Instant updatedAt
    ) {
        this.id = id;
        this.sourceSnapshotId = sourceSnapshotId;
        this.integrationId = integrationId;
        this.tenantId = tenantId;
        this.accountId = accountId;
        this.mappingVersion = mappingVersion;
        this.status = status;
        this.factCount = factCount;
        this.quarantinedCount = quarantinedCount;
        this.checksum = checksum;
        this.producedAt = producedAt;
        this.failedAt = failedAt;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CanonicalBatch create(
        UUID sourceSnapshotId, UUID integrationId, UUID tenantId,
        String accountId, MappingVersion mappingVersion, Instant now
    ) {
        UUID id = deterministicId(sourceSnapshotId, mappingVersion);
        CanonicalBatch batch = new CanonicalBatch(
            id, sourceSnapshotId, integrationId, tenantId, accountId, mappingVersion,
            BatchStatus.PROCESSING, 0, 0, null, null, null, null, now, now
        );
        batch.events.add(new CanonicalBatchStarted(id, sourceSnapshotId, tenantId, mappingVersion.value(), now));
        return batch;
    }

    public static CanonicalBatch reconstitute(
        UUID id, UUID sourceSnapshotId, UUID integrationId, UUID tenantId,
        String accountId, MappingVersion mappingVersion, BatchStatus status,
        int factCount, int quarantinedCount, String checksum,
        Instant producedAt, Instant failedAt, String failureReason,
        Instant createdAt, Instant updatedAt
    ) {
        return new CanonicalBatch(
            id, sourceSnapshotId, integrationId, tenantId, accountId, mappingVersion, status,
            factCount, quarantinedCount, checksum, producedAt, failedAt, failureReason,
            createdAt, updatedAt
        );
    }

    public void produce(int factCount, int quarantinedCount, String checksum, Instant now) {
        if (this.status == BatchStatus.PRODUCED) {
            throw new IllegalStateException("Batch " + id + " is already PRODUCED and immutable");
        }
        this.status = BatchStatus.PRODUCED;
        this.factCount = factCount;
        this.quarantinedCount = quarantinedCount;
        this.checksum = checksum;
        this.producedAt = now;
        this.updatedAt = now;
        events.add(new CanonicalBatchProduced(
            id, tenantId, "GOOGLE_ADS",
            List.of(sourceSnapshotId), mappingVersion.value(),
            factCount, quarantinedCount, checksum, now
        ));
    }

    public void fail(String reason, Instant now) {
        this.status = BatchStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = now;
        this.updatedAt = now;
        events.add(new CanonicalBatchFailed(id, sourceSnapshotId, tenantId, reason, now));
    }

    public void rebuild(Instant now) {
        this.status = BatchStatus.REBUILT;
        this.updatedAt = now;
        events.add(new CanonicalBatchRebuilt(id, sourceSnapshotId, tenantId, mappingVersion.value(), now));
    }

    private static UUID deterministicId(UUID snapshotId, MappingVersion version) {
        String seed = snapshotId + ":" + version.value();
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // Use first 16 bytes as UUID
            byte[] uuidBytes = new byte[16];
            System.arraycopy(hash, 0, uuidBytes, 0, 16);
            return UUID.nameUUIDFromBytes(uuidBytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public UUID getId() { return id; }
    public UUID getSourceSnapshotId() { return sourceSnapshotId; }
    public UUID getIntegrationId() { return integrationId; }
    public UUID getTenantId() { return tenantId; }
    public String getAccountId() { return accountId; }
    public MappingVersion getMappingVersion() { return mappingVersion; }
    public BatchStatus getStatus() { return status; }
    public int getFactCount() { return factCount; }
    public int getQuarantinedCount() { return quarantinedCount; }
    public String getChecksum() { return checksum; }
    public Instant getProducedAt() { return producedAt; }
    public Instant getFailedAt() { return failedAt; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Object> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}
