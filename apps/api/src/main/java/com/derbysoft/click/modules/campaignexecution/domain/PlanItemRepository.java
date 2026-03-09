package com.derbysoft.click.modules.campaignexecution.domain;

import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanItemRepository {
    Optional<PlanItem> findById(UUID id);
    List<PlanItem> findByRevisionId(UUID revisionId);
    List<PlanItem> findQueuedItems(Instant now);
    List<PlanItem> findByRevisionIdAndStatusIn(UUID revisionId, List<PlanItemStatus> statuses);
    PlanItem save(PlanItem item);
    List<PlanItem> saveAll(List<PlanItem> items);
}
