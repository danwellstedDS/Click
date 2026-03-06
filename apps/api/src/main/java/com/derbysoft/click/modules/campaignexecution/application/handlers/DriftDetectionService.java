package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.DriftReport;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * MVP stub — returns empty list. Real drift detection deferred to Phase 2.
 */
@Service
public class DriftDetectionService {

    public List<DriftReport> detect(UUID revisionId, UUID tenantId) {
        return List.of();
    }
}
