package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity.GoogleConnectionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleConnectionJpaRepository extends JpaRepository<GoogleConnectionEntity, UUID> {
    Optional<GoogleConnectionEntity> findByTenantId(UUID tenantId);
    List<GoogleConnectionEntity> findAllByStatus(String status);
}
