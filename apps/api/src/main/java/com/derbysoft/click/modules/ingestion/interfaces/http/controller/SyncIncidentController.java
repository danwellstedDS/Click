package com.derbysoft.click.modules.ingestion.interfaces.http.controller;

import com.derbysoft.click.modules.ingestion.api.contracts.SyncIncidentInfo;
import com.derbysoft.click.modules.ingestion.api.ports.IngestionQueryPort;
import com.derbysoft.click.modules.ingestion.application.handlers.IngestionJobService;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncIncident;
import com.derbysoft.click.modules.ingestion.interfaces.http.dto.AcknowledgeRequest;
import com.derbysoft.click.modules.ingestion.interfaces.http.dto.SyncIncidentResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingestion/incidents")
public class SyncIncidentController {

    private final IngestionJobService ingestionJobService;
    private final IngestionQueryPort ingestionQueryPort;

    public SyncIncidentController(IngestionJobService ingestionJobService,
                                   IngestionQueryPort ingestionQueryPort) {
        this.ingestionJobService = ingestionJobService;
        this.ingestionQueryPort = ingestionQueryPort;
    }

    @GetMapping
    public ApiResponse<List<SyncIncidentResponse>> listOpenIncidents(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<SyncIncidentResponse> incidents = ingestionQueryPort.listOpenIncidents(tenantId).stream()
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(incidents, requestId(request));
    }

    @GetMapping("/escalated")
    public ApiResponse<List<SyncIncidentResponse>> listEscalatedIncidents(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<SyncIncidentResponse> incidents = ingestionQueryPort.listEscalatedIncidents(tenantId).stream()
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(incidents, requestId(request));
    }

    @PostMapping("/{id}/acknowledge")
    public ApiResponse<SyncIncidentResponse> acknowledge(
        @PathVariable UUID id,
        @RequestBody @Valid AcknowledgeRequest body,
        HttpServletRequest request
    ) {
        String by = extractTriggeredBy(request);
        SyncIncident incident = ingestionJobService.acknowledgeEscalation(id, body.ackReason(), by);
        return ApiResponse.success(toResponse(incident), requestId(request));
    }

    private SyncIncidentResponse toResponse(SyncIncidentInfo info) {
        return new SyncIncidentResponse(
            info.id(), info.idempotencyKey(), info.tenantId(), info.failureClass(),
            info.status(), info.consecutiveFailures(), info.lastFailedAt(),
            info.acknowledgedBy(), info.ackReason()
        );
    }

    private SyncIncidentResponse toResponse(SyncIncident incident) {
        return new SyncIncidentResponse(
            incident.getId(), incident.getIdempotencyKey(), incident.getTenantId(),
            incident.getFailureClass().name(), incident.getStatus().name(),
            incident.getConsecutiveFailures(), incident.getLastFailedAt(),
            incident.getAcknowledgedBy(), incident.getAckReason()
        );
    }

    private String extractTriggeredBy(HttpServletRequest request) {
        Object principal = request.getAttribute("userId");
        return principal != null ? principal.toString() : "system";
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? "unknown" : value.toString();
    }
}
