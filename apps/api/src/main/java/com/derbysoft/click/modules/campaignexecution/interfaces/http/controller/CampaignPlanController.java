package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import com.derbysoft.click.modules.campaignexecution.application.handlers.CampaignPlanService;
import com.derbysoft.click.modules.campaignexecution.application.handlers.CampaignPlanService.PlanItemDraft;
import com.derbysoft.click.modules.campaignexecution.application.handlers.PlanApplyService;
import com.derbysoft.click.modules.campaignexecution.domain.CampaignPlanRepository;
import com.derbysoft.click.modules.campaignexecution.domain.PlanRevisionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.CampaignPlan;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.PlanRevision;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.ApplyOrder;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.WriteActionType;
import com.derbysoft.click.modules.campaignexecution.domain.errors.RateLimitExceededException;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.ApplyRevisionRequest;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.CampaignPlanResponse;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.CancelRevisionRequest;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.CreateCampaignPlanRequest;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.PlanRevisionResponse;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.PublishRevisionRequest;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.SaveDraftRevisionRequest;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campaign-plans")
public class CampaignPlanController {

    private final CampaignPlanService campaignPlanService;
    private final PlanApplyService planApplyService;
    private final CampaignPlanRepository campaignPlanRepository;
    private final PlanRevisionRepository planRevisionRepository;

    public CampaignPlanController(CampaignPlanService campaignPlanService,
                                   PlanApplyService planApplyService,
                                   CampaignPlanRepository campaignPlanRepository,
                                   PlanRevisionRepository planRevisionRepository) {
        this.campaignPlanService = campaignPlanService;
        this.planApplyService = planApplyService;
        this.campaignPlanRepository = campaignPlanRepository;
        this.planRevisionRepository = planRevisionRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignPlanResponse>> createPlan(
        @RequestBody @Valid CreateCampaignPlanRequest body,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        String by = extractTriggeredBy(request);
        CampaignPlan plan = campaignPlanService.createPlan(tenantId, body.name(),
            body.description(), by);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(toResponse(plan), requestId(request)));
    }

    @GetMapping
    public ApiResponse<List<CampaignPlanResponse>> listByTenant(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<CampaignPlanResponse> plans = campaignPlanRepository.findByTenantId(tenantId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(plans, requestId(request));
    }

    @GetMapping("/{planId}")
    public ApiResponse<CampaignPlanResponse> getPlan(
        @PathVariable UUID planId,
        HttpServletRequest request
    ) {
        CampaignPlan plan = campaignPlanRepository.findById(planId)
            .orElseThrow(() -> new com.derbysoft.click.sharedkernel.domain.errors.DomainError.NotFound(
                "CE_404", "CampaignPlan not found: " + planId));
        return ApiResponse.success(toResponse(plan), requestId(request));
    }

    @PostMapping("/{planId}/revisions")
    public ApiResponse<PlanRevisionResponse> saveDraftRevision(
        @PathVariable UUID planId,
        @RequestBody @Valid SaveDraftRevisionRequest body,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        String by = extractTriggeredBy(request);
        List<PlanItemDraft> drafts = body.items().stream()
            .map(d -> new PlanItemDraft(
                WriteActionType.valueOf(d.actionType()),
                d.resourceType(),
                d.resourceId(),
                d.payload(),
                ApplyOrder.valueOf(d.applyOrder())
            ))
            .toList();
        PlanRevision revision = campaignPlanService.saveDraftRevision(planId, tenantId, drafts, by);
        return ApiResponse.success(toResponse(revision), requestId(request));
    }

    @GetMapping("/{planId}/revisions")
    public ApiResponse<List<PlanRevisionResponse>> listRevisions(
        @PathVariable UUID planId,
        HttpServletRequest request
    ) {
        List<PlanRevisionResponse> revisions = planRevisionRepository.findByPlanId(planId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(revisions, requestId(request));
    }

    @GetMapping("/{planId}/revisions/{revId}")
    public ApiResponse<PlanRevisionResponse> getRevision(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        HttpServletRequest request
    ) {
        PlanRevision revision = planRevisionRepository.findById(revId)
            .orElseThrow(() -> new com.derbysoft.click.sharedkernel.domain.errors.DomainError.NotFound(
                "CE_404", "PlanRevision not found: " + revId));
        return ApiResponse.success(toResponse(revision), requestId(request));
    }

    @PostMapping("/{planId}/revisions/{revId}/publish")
    public ApiResponse<PlanRevisionResponse> publishRevision(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        @RequestBody(required = false) PublishRevisionRequest body,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        String by = extractTriggeredBy(request);
        PlanRevision revision = campaignPlanService.publishRevision(revId, tenantId, by);
        return ApiResponse.success(toResponse(revision), requestId(request));
    }

    @PostMapping("/{planId}/revisions/{revId}/apply")
    public ResponseEntity<ApiResponse<PlanRevisionResponse>> applyRevision(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        @RequestBody @Valid ApplyRevisionRequest body,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        String by = extractTriggeredBy(request);
        try {
            PlanRevision revision = planApplyService.applyRevision(revId, tenantId, by, body.reason());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(toResponse(revision), requestId(request)));
        } catch (RateLimitExceededException e) {
            // Gap #7: return 429 with Retry-After header (seconds)
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfter().getSeconds()));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(ApiResponse.error("CE_429", e.getMessage(), requestId(request)));
        }
    }

    @PostMapping("/{planId}/revisions/{revId}/cancel")
    public ApiResponse<PlanRevisionResponse> cancelRevision(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        @RequestBody @Valid CancelRevisionRequest body,
        HttpServletRequest request
    ) {
        String by = extractTriggeredBy(request);
        PlanRevision revision = campaignPlanService.cancelRevision(revId, by, body.reason());
        return ApiResponse.success(toResponse(revision), requestId(request));
    }

    private CampaignPlanResponse toResponse(CampaignPlan plan) {
        return new CampaignPlanResponse(plan.getId(), plan.getTenantId(), plan.getName(),
            plan.getDescription(), plan.getCreatedAt(), plan.getUpdatedAt());
    }

    private PlanRevisionResponse toResponse(PlanRevision revision) {
        return new PlanRevisionResponse(revision.getId(), revision.getPlanId(),
            revision.getTenantId(), revision.getRevisionNumber(), revision.getStatus().name(),
            revision.getPublishedBy(), revision.getPublishedAt(),
            revision.getCreatedAt(), revision.getUpdatedAt());
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
