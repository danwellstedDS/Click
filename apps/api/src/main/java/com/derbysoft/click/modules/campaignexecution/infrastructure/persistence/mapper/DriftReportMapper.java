package com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.mapper;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.DriftReport;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.DriftSeverity;
import com.derbysoft.click.modules.campaignexecution.infrastructure.persistence.entity.DriftReportEntity;
import org.springframework.stereotype.Component;

@Component
public class DriftReportMapper {

    public DriftReport toDomain(DriftReportEntity entity) {
        return DriftReport.create(
            entity.getId(),
            entity.getPlanId(),
            entity.getRevisionId(),
            entity.getTenantId(),
            DriftSeverity.valueOf(entity.getSeverity()),
            entity.getResourceType(),
            entity.getResourceId(),
            entity.getField(),
            entity.getIntendedValue(),
            entity.getProviderValue(),
            entity.getDetectedAt()
        );
    }

    public DriftReportEntity toEntity(DriftReport domain) {
        return new DriftReportEntity(
            domain.getId(),
            domain.getPlanId(),
            domain.getRevisionId(),
            domain.getTenantId(),
            domain.getSeverity().name(),
            domain.getResourceType(),
            domain.getResourceId(),
            domain.getField(),
            domain.getIntendedValue(),
            domain.getProviderValue(),
            domain.getDetectedAt()
        );
    }
}
