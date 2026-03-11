package com.derbysoft.click.modules.attributionmapping.domain;

import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingRun;
import java.util.Optional;
import java.util.UUID;

public interface MappingRunRepository {
    MappingRun save(MappingRun run);
    Optional<MappingRun> findById(UUID id);
    Optional<MappingRun> findByCanonicalBatchIdAndRuleSetVersionAndOverrideSetVersion(
        UUID canonicalBatchId, String ruleSetVersion, String overrideSetVersion);
}
