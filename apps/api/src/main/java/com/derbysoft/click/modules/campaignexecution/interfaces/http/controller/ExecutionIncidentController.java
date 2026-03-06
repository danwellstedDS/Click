package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import com.derbysoft.click.modules.campaignexecution.api.ports.CampaignManagementQueryPort;
import com.derbysoft.click.modules.campaignexecution.application.handlers.AcknowledgeEscalationHandler;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.AcknowledgeEscalationRequest;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.ExecutionIncidentResponse;
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
@RequestMapping("/api/v1/execution/incidents")
public class ExecutionIncidentController {

    private final CampaignManagementQueryPort queryPort;
    private final AcknowledgeEscalationHandler acknowledgeHandler;

    public ExecutionIncidentController(CampaignManagementQueryPort queryPort,
                                        AcknowledgeEscalationHandler acknowledgeHandler) {
        this.queryPort = queryPort;
        this.acknowledgeHandler = acknowledgeHandler;
    }

    @GetMapping
    public ApiResponse<List<ExecutionIncidentResponse>> listOpen(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<ExecutionIncidentResponse> incidents = queryPort.listOpenIncidents(tenantId).stream()
            .map(i -> new ExecutionIncidentResponse(
                i.incidentId(), i.idempotencyKey(), i.tenantId(),
                i.failureClass(), i.status(), i.consecutiveFailures(),
                i.lastFailedAt(), i.acknowledgedBy(), i.ackReason()
            ))
            .toList();
        return ApiResponse.success(incidents, requestId(request));
    }

    @GetMapping("/escalated")
    public ApiResponse<List<ExecutionIncidentResponse>> listEscalated(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<ExecutionIncidentResponse> incidents = queryPort.listEscalatedIncidents(tenantId).stream()
            .map(i -> new ExecutionIncidentResponse(
                i.incidentId(), i.idempotencyKey(), i.tenantId(),
                i.failureClass(), i.status(), i.consecutiveFailures(),
                i.lastFailedAt(), i.acknowledgedBy(), i.ackReason()
            ))
            .toList();
        return ApiResponse.success(incidents, requestId(request));
    }

    @PostMapping("/{id}/acknowledge")
    public ApiResponse<ExecutionIncidentResponse> acknowledge(
        @PathVariable UUID id,
        @RequestBody @Valid AcknowledgeEscalationRequest body,
        HttpServletRequest request
    ) {
        String by = extractTriggeredBy(request);
        ExecutionIncident incident = acknowledgeHandler.acknowledge(id, body.ackReason(), by);
        return ApiResponse.success(toResponse(incident), requestId(request));
    }

    private ExecutionIncidentResponse toResponse(ExecutionIncident incident) {
        return new ExecutionIncidentResponse(
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
