package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository;

import com.derbysoft.click.modules.googleadsmanagement.domain.entities.AccountGraphState;
import java.util.List;
import java.util.UUID;

/**
 * Infra-level repository for AccountGraphState.
 * Not a domain port — used directly by DiscoverAccountsHandler.
 */
public interface AccountGraphStateRepository {
    List<AccountGraphState> findByConnectionId(UUID connectionId);
    void deleteByConnectionId(UUID connectionId);
    AccountGraphState save(AccountGraphState state);
}
