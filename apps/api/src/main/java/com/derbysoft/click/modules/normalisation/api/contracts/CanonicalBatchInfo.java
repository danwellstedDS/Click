package com.derbysoft.click.modules.normalisation.api.contracts;

import java.time.Instant;
import java.util.UUID;

public record CanonicalBatchInfo(
    UUID id,
    UUID sourceSnapshotId,
    UUID integrationId,
    UUID tenantId,
    String accountId,
    String mappingVersion,
    String status,
    int factCount,
    int quarantinedCount,
    String checksum,
    Instant producedAt,
    Instant failedAt,
    String failureReason,
    Instant createdAt
) {}
