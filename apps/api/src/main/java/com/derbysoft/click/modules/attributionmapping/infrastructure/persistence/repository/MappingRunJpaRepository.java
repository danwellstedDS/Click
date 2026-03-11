package com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingRunEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingRunJpaRepository extends JpaRepository<MappingRunEntity, UUID> {

    Optional<MappingRunEntity> findByCanonicalBatchIdAndRuleSetVersionAndOverrideSetVersion(
        UUID canonicalBatchId, String ruleSetVersion, String overrideSetVersion);

    List<MappingRunEntity> findByTenantId(UUID tenantId, Pageable pageable);
}
