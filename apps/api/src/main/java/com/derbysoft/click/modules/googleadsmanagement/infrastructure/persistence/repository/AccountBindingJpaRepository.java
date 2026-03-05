package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity.AccountBindingEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBindingJpaRepository extends JpaRepository<AccountBindingEntity, UUID> {
    List<AccountBindingEntity> findByConnectionId(UUID connectionId);
    List<AccountBindingEntity> findByTenantId(UUID tenantId);
    Optional<AccountBindingEntity> findByConnectionIdAndCustomerId(UUID connectionId, String customerId);
}
