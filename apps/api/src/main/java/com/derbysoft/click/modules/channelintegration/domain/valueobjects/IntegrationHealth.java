package com.derbysoft.click.modules.channelintegration.domain.valueobjects;

import java.time.Instant;

public record IntegrationHealth(
    Instant lastSyncAt,
    SyncStatus lastSyncStatus,
    Instant lastSuccessAt,
    String lastErrorCode,
    String lastErrorMessage,
    int consecutiveFailures,
    String statusReason
) {
    public static IntegrationHealth empty() {
        return new IntegrationHealth(null, SyncStatus.NEVER, null, null, null, 0, null);
    }

    public IntegrationHealth withSuccess(Instant syncAt) {
        return new IntegrationHealth(syncAt, SyncStatus.SUCCESS, syncAt, null, null, 0, null);
    }

    public IntegrationHealth withFailure(Instant failedAt, String errorCode, String errorMessage) {
        return new IntegrationHealth(
            failedAt,
            SyncStatus.FAILED,
            lastSuccessAt,   // preserve last known success time
            errorCode,
            errorMessage,
            consecutiveFailures + 1,
            statusReason
        );
    }
}
