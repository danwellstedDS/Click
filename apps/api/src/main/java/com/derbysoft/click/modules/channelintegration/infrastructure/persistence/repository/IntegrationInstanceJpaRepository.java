package com.derbysoft.click.modules.channelintegration.infrastructure.persistence.repository;

import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.entity.IntegrationInstanceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationInstanceJpaRepository extends JpaRepository<IntegrationInstanceEntity, UUID> {
    Optional<IntegrationInstanceEntity> findByTenantIdAndChannel(UUID tenantId, String channel);
    List<IntegrationInstanceEntity> findAllByTenantId(UUID tenantId);
    List<IntegrationInstanceEntity> findAllByStatus(String status);
}
