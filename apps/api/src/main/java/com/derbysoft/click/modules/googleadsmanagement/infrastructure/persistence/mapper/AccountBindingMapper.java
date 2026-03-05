package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingStatus;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity.AccountBindingEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountBindingMapper {

    public AccountBinding toDomain(AccountBindingEntity entity) {
        return AccountBinding.reconstitute(
            entity.getId(),
            entity.getConnectionId(),
            entity.getTenantId(),
            entity.getCustomerId(),
            BindingStatus.valueOf(entity.getStatus()),
            BindingType.valueOf(entity.getBindingType()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public AccountBindingEntity toEntity(AccountBinding domain) {
        return new AccountBindingEntity(
            domain.getId(),
            domain.getConnectionId(),
            domain.getTenantId(),
            domain.getCustomerId(),
            domain.getStatus().name(),
            domain.getBindingType().name()
        );
    }
}
