package com.derbysoft.click.modules.ingestion.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.ingestion.domain.aggregates.RawSnapshot;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.entity.RawSnapshotEntity;
import org.springframework.stereotype.Component;

@Component
public class RawSnapshotMapper {

    public RawSnapshot toDomain(RawSnapshotEntity entity) {
        return RawSnapshot.reconstitute(
            entity.getId(),
            entity.getSyncJobId(),
            entity.getIntegrationId(),
            entity.getTenantId(),
            entity.getAccountId(),
            entity.getReportType(),
            new DateWindow(entity.getDateFrom(), entity.getDateTo()),
            entity.getRowCount(),
            entity.getChecksum(),
            entity.getCreatedAt()
        );
    }

    public RawSnapshotEntity toEntity(RawSnapshot domain) {
        return new RawSnapshotEntity(
            domain.getId(),
            domain.getSyncJobId(),
            domain.getIntegrationId(),
            domain.getTenantId(),
            domain.getAccountId(),
            domain.getReportType(),
            domain.getDateWindow().from(),
            domain.getDateWindow().to(),
            domain.getRowCount(),
            domain.getChecksum()
        );
    }
}
