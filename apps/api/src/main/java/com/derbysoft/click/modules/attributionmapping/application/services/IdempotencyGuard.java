package com.derbysoft.click.modules.attributionmapping.application.services;

import com.derbysoft.click.modules.attributionmapping.domain.MappingRunRepository;
import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingRun;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.RunStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyGuard {

    private final MappingRunRepository runRepository;

    public IdempotencyGuard(MappingRunRepository runRepository) {
        this.runRepository = runRepository;
    }

    /**
     * Returns existing run if already PRODUCED (idempotent success), empty if not yet processed.
     * Throws if RUNNING (concurrent race).
     */
    public Optional<MappingRun> check(
        UUID canonicalBatchId, String ruleSetVersion, String overrideSetVersion
    ) {
        return runRepository.findByCanonicalBatchIdAndRuleSetVersionAndOverrideSetVersion(
            canonicalBatchId, ruleSetVersion, overrideSetVersion
        ).map(existing -> {
            if (existing.getStatus() == RunStatus.RUNNING) {
                throw new IllegalStateException(
                    "MappingRun for batch " + canonicalBatchId + " is already RUNNING — possible concurrent execution");
            }
            return existing;
        });
    }
}
