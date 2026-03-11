package com.derbysoft.click.modules.normalisation.domain;

import com.derbysoft.click.modules.normalisation.domain.aggregates.CanonicalBatch;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import java.util.Optional;
import java.util.UUID;

public interface CanonicalBatchRepository {
    CanonicalBatch save(CanonicalBatch batch);
    Optional<CanonicalBatch> findById(UUID id);
    Optional<CanonicalBatch> findBySourceSnapshotIdAndMappingVersion(UUID snapshotId, MappingVersion version);
}
