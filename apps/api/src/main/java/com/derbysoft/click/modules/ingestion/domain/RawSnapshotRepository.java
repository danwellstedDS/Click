package com.derbysoft.click.modules.ingestion.domain;

import com.derbysoft.click.modules.ingestion.domain.aggregates.RawSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RawSnapshotRepository {
    Optional<RawSnapshot> findById(UUID id);
    List<RawSnapshot> findByJobId(UUID jobId);
    RawSnapshot save(RawSnapshot snapshot);
}
