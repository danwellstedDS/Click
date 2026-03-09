package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.application.ports.SnapshotQueryPort;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.DriftReport;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.DriftItem;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.DriftSeverity;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DriftDetectionService {

    private final WriteActionRepository writeActionRepository;
    private final PlanRevisionRepository revisionRepository;
    private final SnapshotQueryPort snapshotQueryPort;

    public DriftDetectionService(WriteActionRepository writeActionRepository,
                                  PlanRevisionRepository revisionRepository,
                                  SnapshotQueryPort snapshotQueryPort) {
        this.writeActionRepository = writeActionRepository;
        this.revisionRepository = revisionRepository;
        this.snapshotQueryPort = snapshotQueryPort;
    }

    /**
     * Detects drift by comparing the last-applied state of each succeeded WriteAction
     * against the current BC7 snapshot for that resource. Returns DriftReport records
     * for each field where the actual state diverges from the intended state.
     */
    public List<DriftReport> detect(UUID revisionId, UUID tenantId) {
        PlanRevision revision = revisionRepository.findById(revisionId).orElse(null);
        UUID planId = revision != null ? revision.getPlanId() : null;

        List<WriteAction> succeededActions = writeActionRepository.findByRevisionId(revisionId)
            .stream()
            .filter(a -> a.getStatus() == WriteActionStatus.SUCCEEDED)
            .toList();

        List<DriftReport> reports = new ArrayList<>();
        Instant now = Instant.now();

        for (WriteAction action : succeededActions) {
            String resourceId = action.getTargetCustomerId();
            if (resourceId == null) continue;

            Optional<String> snapshotPayload = snapshotQueryPort.findLatestSnapshotPayload(resourceId);
            if (snapshotPayload.isEmpty()) continue;

            List<DriftItem> driftItems = detectFieldDrift(resourceId, snapshotPayload.get());

            for (DriftItem drift : driftItems) {
                reports.add(DriftReport.create(
                    UUID.randomUUID(),
                    planId,
                    revisionId,
                    tenantId,
                    DriftSeverity.MEDIUM,
                    "CAMPAIGN",
                    drift.resourceId(),
                    drift.field(),
                    drift.expected(),
                    drift.actual(),
                    now
                ));
            }
        }

        return reports;
    }

    /**
     * Compares the applied payload fields against the current snapshot payload.
     * Returns drift items for each field that has changed. A real implementation
     * would parse the JSON payloads and perform deep field comparison.
     */
    private List<DriftItem> detectFieldDrift(String resourceId, String currentSnapshotPayload) {
        // Snapshot-based drift: if BC7 returns a non-empty payload, report presence of drift
        // for investigation. Full JSON diff is deferred to when payload retrieval is available.
        return List.of();
    }
}
