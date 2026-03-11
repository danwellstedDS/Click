package com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository;

import com.derbysoft.click.modules.normalisation.infrastructure.persistence.entity.CanonicalBatchEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanonicalBatchJpaRepository extends JpaRepository<CanonicalBatchEntity, UUID> {

    Optional<CanonicalBatchEntity> findBySourceSnapshotIdAndMappingVersion(UUID sourceSnapshotId, String mappingVersion);

    List<CanonicalBatchEntity> findByTenantId(UUID tenantId, Pageable pageable);

    List<CanonicalBatchEntity> findByTenantIdAndStatus(UUID tenantId, String status, Pageable pageable);

    boolean existsBySourceSnapshotIdAndMappingVersion(UUID sourceSnapshotId, String mappingVersion);
}
