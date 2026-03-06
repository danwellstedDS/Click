package com.derbysoft.click.modules.campaignexecution.interfaces.http.controller;

import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.interfaces.http.dto.WriteActionResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/execution/queue")
public class WriteActionQueueController {

    private final WriteActionRepository writeActionRepository;

    public WriteActionQueueController(WriteActionRepository writeActionRepository) {
        this.writeActionRepository = writeActionRepository;
    }

    @GetMapping
    public ApiResponse<List<WriteActionResponse>> listQueueByTenant(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<WriteActionResponse> actions = writeActionRepository.findPendingActions(Instant.now())
            .stream()
            .filter(a -> a.getTenantId().equals(tenantId))
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(actions, requestId(request));
    }

    @GetMapping("/{revisionId}")
    public ApiResponse<List<WriteActionResponse>> listActionsByRevision(
        @PathVariable UUID revisionId,
        HttpServletRequest request
    ) {
        List<WriteActionResponse> actions = writeActionRepository.findByRevisionId(revisionId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(actions, requestId(request));
    }

    private WriteActionResponse toResponse(WriteAction action) {
        return new WriteActionResponse(
            action.getId(), action.getRevisionId(), action.getItemId(),
            action.getActionType().name(), action.getIdempotencyKey(), action.getStatus().name(),
            action.getAttempts(), action.getTriggerType().name(),
            action.getFailureClass() != null ? action.getFailureClass().name() : null,
            action.getFailureReason(),
            action.getCreatedAt(), action.getUpdatedAt()
        );
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? "unknown" : value.toString();
    }
}
