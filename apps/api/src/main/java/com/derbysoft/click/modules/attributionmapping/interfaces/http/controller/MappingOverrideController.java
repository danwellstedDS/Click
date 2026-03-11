package com.derbysoft.click.modules.attributionmapping.interfaces.http.controller;

import com.derbysoft.click.modules.attributionmapping.application.handlers.AttributionService;
import com.derbysoft.click.modules.attributionmapping.domain.MappingOverrideRepository;
import com.derbysoft.click.modules.attributionmapping.domain.aggregates.MappingOverride;
import com.derbysoft.click.modules.attributionmapping.domain.valueobjects.OverrideScope;
import com.derbysoft.click.modules.attributionmapping.infrastructure.persistence.entity.MappingOverrideEntity;
import com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.request.RemoveMappingOverrideRequest;
import com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.request.SetMappingOverrideRequest;
import com.derbysoft.click.modules.attributionmapping.interfaces.http.dto.response.MappingOverrideResponse;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mapping-overrides")
public class MappingOverrideController {

    private final MappingOverrideRepository overrideRepository;

    public MappingOverrideController(MappingOverrideRepository overrideRepository) {
        this.overrideRepository = overrideRepository;
    }

    @GetMapping
    public ApiResponse<List<MappingOverrideResponse>> listOverrides(HttpServletRequest request) {
        UUID tenantId = extractTenantId(request);
        List<MappingOverrideResponse> overrides = overrideRepository.findActiveByTenantId(tenantId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(overrides, requestId(request));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MappingOverrideResponse>> setOverride(
        @RequestBody SetMappingOverrideRequest req,
        HttpServletRequest request
    ) {
        UUID tenantId = extractTenantId(request);
        OverrideScope scope = req.campaignId() != null && !req.campaignId().isBlank()
            ? OverrideScope.ACCOUNT_CAMPAIGN
            : OverrideScope.ACCOUNT;

        MappingOverride override = MappingOverride.set(
            tenantId, scope, req.customerAccountId(), req.campaignId(),
            req.targetOrgNodeId(), req.targetScopeType(),
            req.reason(), "system", Instant.now()
        );
        MappingOverride saved = overrideRepository.save(override);
        MappingOverrideResponse response = toEntityResponse(saved);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, requestId(request)));
    }

    @DeleteMapping("/{overrideId}")
    public ResponseEntity<ApiResponse<Void>> removeOverride(
        @PathVariable UUID overrideId,
        @RequestBody RemoveMappingOverrideRequest req,
        HttpServletRequest request
    ) {
        return overrideRepository.findById(overrideId)
            .map(override -> {
                override.remove(req.reason(), "system", Instant.now());
                overrideRepository.save(override);
                return ResponseEntity.ok(ApiResponse.<Void>success(null, requestId(request)));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    private MappingOverrideResponse toResponse(MappingOverrideEntity e) {
        return new MappingOverrideResponse(
            e.getId(), e.getTenantId(), e.getScopeType(),
            e.getCustomerAccountId(), e.getCampaignId(),
            e.getTargetOrgNodeId(), e.getTargetScopeType(),
            e.getReason(), e.getActor(), e.getStatus(),
            e.getRemovedAt(), e.getRemovedReason(), e.getCreatedAt()
        );
    }

    private MappingOverrideResponse toEntityResponse(MappingOverride o) {
        return new MappingOverrideResponse(
            o.getId(), o.getTenantId(), o.getScopeType().name(),
            o.getCustomerAccountId(), o.getCampaignId(),
            o.getTargetOrgNodeId(), o.getTargetScopeType(),
            o.getReason(), o.getActor(), o.getStatus(),
            o.getRemovedAt(), o.getRemovedReason(), o.getCreatedAt()
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
