package com.derbysoft.click.modules.normalisation.api.ports;

import com.derbysoft.click.modules.normalisation.api.contracts.CanonicalBatchInfo;
import com.derbysoft.click.modules.normalisation.api.contracts.CanonicalFactInfo;
import com.derbysoft.click.modules.normalisation.api.contracts.NormalisationQualityStats;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NormalisationQueryPort {
    Optional<CanonicalBatchInfo> findBatchById(UUID batchId);
    List<CanonicalBatchInfo> listBatches(UUID tenantId, String statusFilter, int page, int size);
    List<CanonicalFactInfo> listFacts(UUID batchId, int page, int size);
    NormalisationQualityStats getQualityStats(UUID batchId);
}
