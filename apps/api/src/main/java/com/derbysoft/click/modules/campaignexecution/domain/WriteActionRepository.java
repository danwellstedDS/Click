package com.derbysoft.click.modules.campaignexecution.domain;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WriteActionRepository {
    Optional<WriteAction> findById(UUID id);
    Optional<WriteAction> findByIdempotencyKey(String key);
    List<WriteAction> findPendingActions(Instant now);
    List<WriteAction> findRunningActionsWithExpiredLease(Instant now);
    List<WriteAction> findByRevisionId(UUID revisionId);
    long countManualTriggersSince(UUID tenantId, List<TriggerType> triggerTypes, Instant since);
    WriteAction save(WriteAction action);
}
