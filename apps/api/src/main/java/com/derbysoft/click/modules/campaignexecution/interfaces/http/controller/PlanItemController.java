package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import com.derbysoft.click.modules.campaignexecution.application.handlers.ForceRunPlanItemHandler;
import com.derbysoft.click.modules.campaignexecution.application.handlers.RetryPlanItemHandler;
import com.derbysoft.click.modules.campaignexecution.domain.PlanItemRepository;
import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.entities.PlanItem;
import com.derbysoft.click.modules.campaignexecution.domain.errors.RateLimitExceededException;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.PlanItemStatus;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.ForceRunItemRequest;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.PlanItemResponse;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.RetryItemRequest;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.WriteActionResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campaign-plans/{planId}/revisions/{revId}/items")
public class PlanItemController {

    private final PlanItemRepository planItemRepository;
    private final WriteActionRepository writeActionRepository;
    private final ForceRunPlanItemHandler forceRunHandler;
    private final RetryPlanItemHandler retryHandler;

    public PlanItemController(PlanItemRepository planItemRepository,
                               WriteActionRepository writeActionRepository,
                               ForceRunPlanItemHandler forceRunHandler,
                               RetryPlanItemHandler retryHandler) {
        this.planItemRepository = planItemRepository;
        this.writeActionRepository = writeActionRepository;
        this.forceRunHandler = forceRunHandler;
        this.retryHandler = retryHandler;
    }

    @GetMapping
    public ApiResponse<List<PlanItemResponse>> listItems(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        HttpServletRequest request
    ) {
        List<PlanItemResponse> items = planItemRepository.findByRevisionId(revId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(items, requestId(request));
    }

    @GetMapping("/{itemId}")
    public ApiResponse<PlanItemResponse> getItem(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        @PathVariable UUID itemId,
        HttpServletRequest request
    ) {
        PlanItem item = planItemRepository.findById(itemId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404", "PlanItem not found: " + itemId));
        return ApiResponse.success(toResponse(item), requestId(request));
    }

    @PostMapping("/{itemId}/retry")
    public ResponseEntity<ApiResponse<PlanItemResponse>> retryItem(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        @PathVariable UUID itemId,
        @RequestBody @Valid RetryItemRequest body,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        String by = extractTriggeredBy(request);
        try {
            PlanItem item = retryHandler.retry(itemId, tenantId, body.reason(), by);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(toResponse(item), requestId(request)));
        } catch (RateLimitExceededException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfter().getSeconds()));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(ApiResponse.error("CE_429", e.getMessage(), requestId(request)));
        }
    }

    @PostMapping("/{itemId}/force-run")
    public ResponseEntity<ApiResponse<PlanItemResponse>> forceRunItem(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        @PathVariable UUID itemId,
        @RequestBody @Valid ForceRunItemRequest body,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        String by = extractTriggeredBy(request);
        try {
            PlanItem item = forceRunHandler.forceRun(itemId, tenantId, body.reason(), by);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(toResponse(item), requestId(request)));
        } catch (RateLimitExceededException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfter().getSeconds()));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(ApiResponse.error("CE_429", e.getMessage(), requestId(request)));
        }
    }

    @GetMapping("/{itemId}/explain")
    public ApiResponse<ExplainResponse> explainItem(
        @PathVariable UUID planId,
        @PathVariable UUID revId,
        @PathVariable UUID itemId,
        HttpServletRequest request
    ) {
        PlanItem item = planItemRepository.findById(itemId)
            .orElseThrow(() -> new DomainError.NotFound("CE_404", "PlanItem not found: " + itemId));
        List<WriteActionResponse> actions = writeActionRepository.findByRevisionId(revId).stream()
            .filter(a -> a.getItemId().equals(itemId))
            .map(this::toWriteActionResponse)
            .toList();
        return ApiResponse.success(new ExplainResponse(toResponse(item), actions), requestId(request));
    }

    private PlanItemResponse toResponse(PlanItem item) {
        return new PlanItemResponse(
            item.getId(), item.getRevisionId(),
            item.getActionType().name(), item.getResourceType(), item.getResourceId(),
            item.getStatus().name(), item.getAttempts(),
            item.getFailureClass() != null ? item.getFailureClass().name() : null,
            item.getFailureReason(),
            item.getCreatedAt(), item.getUpdatedAt(),
            computeNextAction(item), computeActionability(item)
        );
    }

    private String computeNextAction(PlanItem item) {
        if (item.getStatus() == PlanItemStatus.SUCCEEDED) return null;
        if (item.getFailureClass() == FailureClass.PERMANENT) return "Review payload and retry";
        if (item.getFailureClass() == FailureClass.TRANSIENT) {
            if (item.canRetry()) return "Scheduled for retry";
            return "Max retries reached — retry manually";
        }
        return null;
    }

    private String computeActionability(PlanItem item) {
        if (item.getStatus() == PlanItemStatus.SUCCEEDED) return "NONE";
        if (item.getFailureClass() == FailureClass.PERMANENT) return "ACTIONABLE";
        if (item.getFailureClass() == FailureClass.TRANSIENT) {
            if (item.canRetry()) return "MONITORING";
            return "ACTIONABLE";
        }
        return "NONE";
    }

    private WriteActionResponse toWriteActionResponse(WriteAction action) {
        return new WriteActionResponse(
            action.getId(), action.getRevisionId(), action.getItemId(),
            action.getActionType().name(), action.getIdempotencyKey(), action.getStatus().name(),
            action.getAttempts(), action.getTriggerType().name(),
            action.getFailureClass() != null ? action.getFailureClass().name() : null,
            action.getFailureReason(),
            action.getCreatedAt(), action.getUpdatedAt()
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

    public record ExplainResponse(PlanItemResponse item, List<WriteActionResponse> actions) {}
}
