package com.derbysoft.click.modules.normalisation.application.services;

import com.derbysoft.click.modules.normalisation.domain.CanonicalBatchRepository;
import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.BatchStatus;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyGuard {

    private final CanonicalBatchRepository batchRepository;

    public IdempotencyGuard(CanonicalBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    /**
     * Returns existing batch if already PRODUCED (idempotent success), empty if not yet processed.
     * Throws if PROCESSING (concurrent race).
     */
    public Optional<CanonicalBatch> check(UUID snapshotId, MappingVersion version) {
        return batchRepository.findBySourceSnapshotIdAndMappingVersion(snapshotId, version)
            .map(existing -> {
                if (existing.getStatus() == BatchStatus.PROCESSING) {
                    throw new IllegalStateException(
                        "Batch for snapshot " + snapshotId + " is already PROCESSING — possible concurrent execution");
                }
                return existing;
            });
    }
}
