package com.derbysoft.click.modules.normalisation.interfaces.http.controller;

import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.RawSnapshotJpaRepository;
import com.derbysoft.click.modules.normalisation.api.contracts.CanonicalBatchInfo;
import com.derbysoft.click.modules.normalisation.api.contracts.CanonicalFactInfo;
import com.derbysoft.click.modules.normalisation.api.ports.NormalisationQueryPort;
import com.derbysoft.click.modules.normalisation.application.handlers.NormalisationService;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.MappingVersion;
import com.derbysoft.click.modules.normalisation.infrastructure.persistence.repository.CanonicalBatchJpaRepository;
import com.derbysoft.click.modules.normalisation.interfaces.http.dto.CanonicalBatchResponse;
import com.derbysoft.click.modules.normalisation.interfaces.http.dto.CanonicalFactResponse;
import com.derbysoft.click.modules.normalisation.interfaces.http.dto.NormalizationQualityReport;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/canonical-batches")
public class CanonicalBatchController {

    private static final Logger log = LoggerFactory.getLogger(CanonicalBatchController.class);

    private final NormalisationQueryPort normalisationQueryPort;
    private final NormalisationService normalisationService;
    private final RawSnapshotJpaRepository rawSnapshotJpaRepository;
    private final CanonicalBatchJpaRepository canonicalBatchJpaRepository;

    public CanonicalBatchController(
        NormalisationQueryPort normalisationQueryPort,
        NormalisationService normalisationService,
        RawSnapshotJpaRepository rawSnapshotJpaRepository,
        CanonicalBatchJpaRepository canonicalBatchJpaRepository
    ) {
        this.normalisationQueryPort = normalisationQueryPort;
        this.normalisationService = normalisationService;
        this.rawSnapshotJpaRepository = rawSnapshotJpaRepository;
        this.canonicalBatchJpaRepository = canonicalBatchJpaRepository;
    }

    @GetMapping
    public ApiResponse<List<CanonicalBatchResponse>> listBatches(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        List<CanonicalBatchResponse> batches = normalisationQueryPort
            .listBatches(tenantId, status, page, size)
            .stream()
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(batches, requestId(request));
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<ApiResponse<CanonicalBatchResponse>> getBatch(
        @PathVariable UUID batchId,
        HttpServletRequest request
    ) {
        return normalisationQueryPort.findBatchById(batchId)
            .map(info -> ResponseEntity.ok(ApiResponse.success(toResponse(info), requestId(request))))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{batchId}/facts")
    public ApiResponse<List<CanonicalFactResponse>> listFacts(
        @PathVariable UUID batchId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        HttpServletRequest request
    ) {
        List<CanonicalFactResponse> facts = normalisationQueryPort.listFacts(batchId, page, size)
            .stream()
            .map(this::toFactResponse)
            .toList();
        return ApiResponse.success(facts, requestId(request));
    }

    @GetMapping("/{batchId}/quality-report")
    public ApiResponse<NormalizationQualityReport> getQualityReport(
        @PathVariable UUID batchId,
        HttpServletRequest request
    ) {
        var stats = normalisationQueryPort.getQualityStats(batchId);
        return ApiResponse.success(
            new NormalizationQualityReport(stats.totalFacts(), stats.quarantinedFacts(), stats.flagBreakdown()),
            requestId(request)
        );
    }

    /**
     * Backfill endpoint: normalises all raw snapshots that do not yet have a canonical batch.
     * Safe to call multiple times — idempotent per snapshot via IdempotencyGuard.
     */
    @PostMapping("/backfill")
    public ApiResponse<Map<String, Integer>> backfill(HttpServletRequest request) {
        AtomicInteger queued = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        rawSnapshotJpaRepository.findAll().forEach(snapshot -> {
            boolean alreadyDone = canonicalBatchJpaRepository
                .existsBySourceSnapshotIdAndMappingVersion(snapshot.getId(), "v1");
            if (alreadyDone) {
                skipped.incrementAndGet();
                return;
            }
            try {
                normalisationService.normalizeSnapshot(
                    snapshot.getId(), snapshot.getIntegrationId(),
                    snapshot.getTenantId(), snapshot.getAccountId(),
                    MappingVersion.V1
                );
                queued.incrementAndGet();
            } catch (Exception e) {
                log.warn("Backfill failed for snapshot {}: {}", snapshot.getId(), e.getMessage());
                failed.incrementAndGet();
            }
        });

        log.info("Backfill complete: {} normalised, {} skipped, {} failed", queued, skipped, failed);
        return ApiResponse.success(
            Map.of("normalised", queued.get(), "skipped", skipped.get(), "failed", failed.get()),
            requestId(request)
        );
    }

    private CanonicalBatchResponse toResponse(CanonicalBatchInfo info) {
        return new CanonicalBatchResponse(
            info.id(), info.sourceSnapshotId(), info.integrationId(), info.tenantId(),
            info.accountId(), info.mappingVersion(), info.status(),
            info.factCount(), info.quarantinedCount(), info.checksum(),
            info.producedAt(), info.failedAt(), info.failureReason(), info.createdAt()
        );
    }

    private CanonicalFactResponse toFactResponse(CanonicalFactInfo info) {
        return new CanonicalFactResponse(
            info.id(), info.canonicalBatchId(), info.sourceSnapshotId(), info.tenantId(),
            info.channel(), info.integrationId(), info.customerAccountId(),
            info.campaignId(), info.campaignName(), info.reportDate(),
            info.impressions(), info.clicks(), info.costMicros(), info.conversions(),
            info.mappingVersion(), info.reconciliationKey(), info.qualityFlags(),
            info.quarantined(), info.ingestedAt()
        );
    }

    private UUID extractTenantId(HttpServletRequest request) {
        Object tenantId = request.getAttribute("tenantId");
        if (tenantId instanceof UUID id) return id;
        String header = request.getHeader("X-Tenant-Id");
        if (header != null) return UUID.fromString(header);
        throw new IllegalStateException("Tenant ID not found in request context");
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? "unknown" : value.toString();
    }
}
