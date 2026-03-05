package com.derbysoft.click.modules.googleadsmanagement.domain;

import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountBindingRepository {
    Optional<AccountBinding> findById(UUID id);
    List<AccountBinding> findByConnectionId(UUID connectionId);
    List<AccountBinding> findByTenantId(UUID tenantId);
    Optional<AccountBinding> findByConnectionIdAndCustomerId(UUID connectionId, String customerId);
    AccountBinding save(AccountBinding binding);
}
