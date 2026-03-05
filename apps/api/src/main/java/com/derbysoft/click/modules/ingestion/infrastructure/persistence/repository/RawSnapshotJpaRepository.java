package com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository;

import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.RawSnapshotEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawSnapshotJpaRepository extends JpaRepository<RawSnapshotEntity, UUID> {
    List<RawSnapshotEntity> findBySyncJobId(UUID syncJobId);
}
