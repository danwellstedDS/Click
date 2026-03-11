package com.derbysoft.click.modules.attributionmapping.interfaces.http.controller;

import com.derbysoft.click.modules.attributionmapping.api.contracts.MappedFactInfo;
import com.derbysoft.click.modules.attributionmapping.api.contracts.MappingRunInfo;
import com.derbysoft.click.modules.attributionmapping.api.ports.AttributionQueryPort;
import com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.response.MappedFactResponse;
import com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.response.MappingRunResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mapping-runs")
public class MappingRunController {

    private final AttributionQueryPort attributionQueryPort;

    public MappingRunController(AttributionQueryPort attributionQueryPort) {
        this.attributionQueryPort = attributionQueryPort;
    }

    @GetMapping
    public ApiResponse<List<MappingRunResponse>> listRuns(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        List<MappingRunResponse> runs = attributionQueryPort.listRuns(tenantId, page, size)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(runs, requestId(request));
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ApiResponse<MappingRunResponse>> getRun(
        @PathVariable UUID runId,
        HttpServletRequest request
    ) {
        return attributionQueryPort.findRunById(runId)
            .map(info -> ResponseEntity.ok(ApiResponse.success(toResponse(info), requestId(request))))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{runId}/facts")
    public ApiResponse<List<MappedFactResponse>> listFacts(
        @PathVariable UUID runId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        HttpServletRequest request
    ) {
        List<MappedFactResponse> facts = attributionQueryPort.listFacts(runId, page, size)
            .stream().map(this::toFactResponse).toList();
        return ApiResponse.success(facts, requestId(request));
    }

    @GetMapping("/{runId}/low-confidence")
    public ApiResponse<List<MappedFactResponse>> listLowConfidence(
        @PathVariable UUID runId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        HttpServletRequest request
    ) {
        List<MappedFactResponse> facts = attributionQueryPort.listLowConfidence(runId, page, size)
            .stream().map(this::toFactResponse).toList();
        return ApiResponse.success(facts, requestId(request));
    }

    private MappingRunResponse toResponse(MappingRunInfo info) {
        return new MappingRunResponse(
            info.id(), info.canonicalBatchId(), info.tenantId(),
            info.ruleSetVersion(), info.overrideSetVersion(), info.status(),
            info.mappedCount(), info.lowConfidenceCount(), info.unresolvedCount(),
            info.startedAt(), info.completedAt(), info.failedAt(), info.failureReason(),
            info.createdAt()
        );
    }

    private MappedFactResponse toFactResponse(MappedFactInfo info) {
        return new MappedFactResponse(
            info.id(), info.mappingRunId(), info.canonicalFactId(), info.tenantId(),
            info.resolvedOrgNodeId(), info.resolvedScopeType(),
            info.confidenceBand(), info.confidenceScore(),
            info.resolutionReasonCode(), info.ruleSetVersion(),
            info.overrideApplied(), info.mappedAt()
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
