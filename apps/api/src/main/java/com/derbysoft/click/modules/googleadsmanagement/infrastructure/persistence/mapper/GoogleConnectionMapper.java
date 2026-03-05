package com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.ConnectionStatus;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.entity.GoogleConnectionEntity;
import org.springframework.stereotype.Component;

@Component
public class GoogleConnectionMapper {

    public GoogleConnection toDomain(GoogleConnectionEntity entity) {
        return GoogleConnection.reconstitute(
            entity.getId(),
            entity.getTenantId(),
            entity.getManagerId(),
            ConnectionStatus.valueOf(entity.getStatus()),
            entity.getCredentialPath(),
            entity.getLastDiscoveredAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public GoogleConnectionEntity toEntity(GoogleConnection domain) {
        return new GoogleConnectionEntity(
            domain.getId(),
            domain.getTenantId(),
            domain.getManagerId(),
            domain.getStatus().name(),
            domain.getCredentialPath(),
            domain.getLastDiscoveredAt()
        );
    }
}
