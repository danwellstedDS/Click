package com.derbysoft.click.modules.channelintegration.infrastructure.persistence.repository;

import com.derbysoft.click.modules.channelintegration.infrastructure.persistence.entity.IntegrationInstanceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationInstanceJpaRepository extends JpaRepository<IntegrationInstanceEntity, UUID> {
    Optional<IntegrationInstanceEntity> findByTenantIdAndChannelAndConnectionKey(UUID tenantId, String channel, String connectionKey);
    List<IntegrationInstanceEntity> findAllByTenantId(UUID tenantId);
    List<IntegrationInstanceEntity> findAllByStatusAndCadenceTypeNot(String status, String cadenceType);
}
