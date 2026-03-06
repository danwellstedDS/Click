package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import com.derbysoft.click.modules.campaignexecution.domain.DriftReportRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.DriftReport;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.DriftReportResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drift-reports")
public class DriftReportController {

    private final DriftReportRepository driftReportRepository;

    public DriftReportController(DriftReportRepository driftReportRepository) {
        this.driftReportRepository = driftReportRepository;
    }

    @GetMapping
    public ApiResponse<List<DriftReportResponse>> listByPlan(
        @RequestParam UUID planId,
        HttpServletRequest request
    ) {
        List<DriftReportResponse> reports = driftReportRepository.findByPlanId(planId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(reports, requestId(request));
    }

    @GetMapping("/revision")
    public ApiResponse<List<DriftReportResponse>> listByRevision(
        @RequestParam UUID revisionId,
        HttpServletRequest request
    ) {
        List<DriftReportResponse> reports = driftReportRepository.findByRevisionId(revisionId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(reports, requestId(request));
    }

    private DriftReportResponse toResponse(DriftReport report) {
        return new DriftReportResponse(
            report.getId(), report.getPlanId(), report.getRevisionId(),
            report.getSeverity().name(), report.getResourceType(), report.getResourceId(),
            report.getField(), report.getIntendedValue(), report.getProviderValue(),
            report.getDetectedAt()
        );
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? "unknown" : value.toString();
    }
}
