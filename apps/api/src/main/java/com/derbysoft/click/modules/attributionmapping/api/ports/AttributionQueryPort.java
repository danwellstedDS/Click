package com.derbysoft.click.modules.attributionmapping.api.ports;

import com.derbysoft.click.modules.attributionmapping.api.contracts.MappedFactInfo;
import com.derbysoft.click.modules.attributionmapping.api.contracts.MappingRunInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributionQueryPort {
    Optional<MappingRunInfo> findRunById(UUID runId);
    List<MappingRunInfo> listRuns(UUID tenantId, int page, int size);
    List<MappedFactInfo> listFacts(UUID runId, int page, int size);
    List<MappedFactInfo> listLowConfidence(UUID runId, int page, int size);
}
