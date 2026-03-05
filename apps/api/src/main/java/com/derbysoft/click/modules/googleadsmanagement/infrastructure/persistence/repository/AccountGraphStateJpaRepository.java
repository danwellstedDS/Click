package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity.AccountGraphStateEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountGraphStateJpaRepository extends JpaRepository<AccountGraphStateEntity, UUID> {
    List<AccountGraphStateEntity> findByConnectionId(UUID connectionId);
    void deleteByConnectionId(UUID connectionId);
}
