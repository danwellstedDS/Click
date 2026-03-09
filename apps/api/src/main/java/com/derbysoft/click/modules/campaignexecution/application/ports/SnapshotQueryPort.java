package com.derbysoft.click.modules.campaignexecution.application.ports;

import java.util.Optional;

/**
 * Port for querying BC7 (ingestion) snapshots by resource ID.
 * Used by DriftDetectionService to compare the last-applied payload
 * against the current Google Ads resource state.
 */
public interface SnapshotQueryPort {

    /**
     * Find the latest snapshot payload for a given resource ID.
     * Returns empty if no snapshot is available.
     */
    Optional<String> findLatestSnapshotPayload(String resourceId);
}
