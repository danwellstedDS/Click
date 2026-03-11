package com.derbysoft.click.modules.normalisation.interfaces.http.dto;

import java.time.Instant;
import java.util.UUID;

public record CanonicalBatchResponse(
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
