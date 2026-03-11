package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.attributionmapping.application.ports.AccountBindingQueryPort;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity.AccountBindingEntity;
import java.util.List;
import java.util.UUID;

/**
 * BC5 → BC9 adapter: exposes BC5's account bindings to BC9's attribution pipeline
 * via {@link AccountBindingQueryPort}.
 */
public class AccountBindingQueryAdapter implements AccountBindingQueryPort {

    private final AccountBindingJpaRepository jpaRepository;

    public AccountBindingQueryAdapter(AccountBindingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ActiveBindingData> findActiveByTenantId(UUID tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
            .filter(e -> "ACTIVE".equals(e.getStatus()))
            .map(this::toData)
            .toList();
    }

    private ActiveBindingData toData(AccountBindingEntity e) {
        return new ActiveBindingData(
            e.getId(), e.getCustomerId(), e.getOrgNodeId(), e.getOrgScopeType()
        );
    }
}
