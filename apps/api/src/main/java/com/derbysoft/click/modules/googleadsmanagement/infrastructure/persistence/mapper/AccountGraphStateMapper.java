package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.googleadsmanagement.domain.entities.AccountGraphState;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity.AccountGraphStateEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountGraphStateMapper {

    public AccountGraphState toDomain(AccountGraphStateEntity entity) {
        return new AccountGraphState(
            entity.getId(),
            entity.getConnectionId(),
            entity.getCustomerId(),
            entity.getAccountName(),
            entity.getCurrencyCode(),
            entity.getTimeZone(),
            entity.getDiscoveredAt()
        );
    }

    public AccountGraphStateEntity toEntity(AccountGraphState domain) {
        return new AccountGraphStateEntity(
            domain.getId(),
            domain.getConnectionId(),
            domain.getCustomerId(),
            domain.getAccountName(),
            domain.getCurrencyCode(),
            domain.getTimeZone(),
            domain.getDiscoveredAt()
        );
    }
}
