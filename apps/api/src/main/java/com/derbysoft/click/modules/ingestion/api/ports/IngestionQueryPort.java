package com.derbysoft.click.modules.ingestion.api.ports;

import com.derbysoft.click.modules.ingestion.api.contracts.SyncIncidentInfo;
import com.derbysoft.click.modules.ingestion.api.contracts.SyncJobInfo;
import java.util.List;
import java.util.UUID;

public interface IngestionQueryPort {
    List<SyncJobInfo> listJobHistory(UUID integrationId);
    List<SyncIncidentInfo> listOpenIncidents(UUID tenantId);
    List<SyncIncidentInfo> listEscalatedIncidents(UUID tenantId);
}
