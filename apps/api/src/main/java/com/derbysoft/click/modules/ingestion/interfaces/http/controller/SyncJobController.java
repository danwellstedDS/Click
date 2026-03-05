package com.derbysoft.click.modules.ingestion.interfaces.http.controller;

import com.derbysoft.click.modules.ingestion.api.ports.IngestionQueryPort;
import com.derbysoft.click.modules.ingestion.application.handlers.IngestionJobService;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.interfaces.http.dto.BackfillRequest;
import com.derbysoft.click.modules.ingestion.interfaces.http.dto.ForceRunRequest;
import com.derbysoft.click.modules.ingestion.interfaces.http.dto.ManualSyncRequest;
import com.derbysoft.click.modules.ingestion.interfaces.http.dto.SyncJobResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingestion/jobs")
public class SyncJobController {

    private final IngestionJobService ingestionJobService;
    private final IngestionQueryPort ingestionQueryPort;

    public SyncJobController(IngestionJobService ingestionJobService,
                              IngestionQueryPort ingestionQueryPort) {
        this.ingestionJobService = ingestionJobService;
        this.ingestionQueryPort = ingestionQueryPort;
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<SyncJobResponse>> manualSync(
        @RequestBody @Valid ManualSyncRequest body,
        HttpServletRequest request
    ) {
        SyncJob job = ingestionJobService.enqueueManualSync(
            extractTenantId(request), body.integrationId(), body.accountId(),
            body.reason(), extractTriggeredBy(request)
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(toResponse(job), requestId(request)));
    }

    @PostMapping("/backfill")
    public ResponseEntity<ApiResponse<SyncJobResponse>> backfill(
        @RequestBody @Valid BackfillRequest body,
        HttpServletRequest request
    ) {
        DateWindow dateWindow = new DateWindow(
            LocalDate.parse(body.dateFrom()),
            LocalDate.parse(body.dateTo())
        );
        SyncJob job = ingestionJobService.enqueueBackfill(
            extractTenantId(request), body.integrationId(), body.accountId(),
            dateWindow, body.reason(), extractTriggeredBy(request)
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(toResponse(job), requestId(request)));
    }

    @PostMapping("/force-run")
    public ResponseEntity<ApiResponse<SyncJobResponse>> forceRun(
        @RequestBody @Valid ForceRunRequest body,
        HttpServletRequest request
    ) {
        SyncJob job = ingestionJobService.forceRun(
            extractTenantId(request), body.integrationId(), body.accountId(),
            body.reason(), extractTriggeredBy(request)
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(toResponse(job), requestId(request)));
    }

    @GetMapping
    public ApiResponse<List<SyncJobResponse>> listJobs(
        @RequestParam UUID integrationId,
        HttpServletRequest request
    ) {
        List<SyncJobResponse> jobs = ingestionQueryPort.listJobHistory(integrationId).stream()
            .map(info -> new SyncJobResponse(
                info.id(), info.integrationId(), info.accountId(), info.reportType(),
                info.dateFrom(), info.dateTo(), info.status(), info.attempts(),
                info.triggerType(), info.failureClass(), info.createdAt()
            ))
            .toList();
        return ApiResponse.success(jobs, requestId(request));
    }

    private SyncJobResponse toResponse(SyncJob job) {
        return new SyncJobResponse(
            job.getId(), job.getIntegrationId(), job.getAccountId(), job.getReportType(),
            job.getDateWindow().from(), job.getDateWindow().to(),
            job.getStatus().name(), job.getAttempts(), job.getTriggerType().name(),
            job.getFailureClass() != null ? job.getFailureClass().name() : null,
            job.getCreatedAt()
        );
    }

    private UUID extractTenantId(HttpServletRequest request) {
        Object tenantId = request.getAttribute("tenantId");
        if (tenantId instanceof UUID id) return id;
        String header = request.getHeader("X-Tenant-Id");
        if (header != null) return UUID.fromString(header);
        throw new IllegalStateException("Tenant ID not found in request context");
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
