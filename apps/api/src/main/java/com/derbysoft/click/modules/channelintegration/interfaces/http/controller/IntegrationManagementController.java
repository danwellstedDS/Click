package com.derbysoft.click.modules.channelintegration.interfaces.http.controller;

import com.derbysoft.click.modules.channelintegration.application.handlers.IntegrationService;
import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.modules.channelintegration.interfaces.http.dto.AttachCredentialRequest;
import com.derbysoft.click.modules.channelintegration.interfaces.http.dto.CreateIntegrationRequest;
import com.derbysoft.click.modules.channelintegration.interfaces.http.dto.IntegrationInstanceResponse;
import com.derbysoft.click.modules.channelintegration.interfaces.http.dto.UpdateScheduleRequest;
import com.derbysoft.click.modules.identityaccess.infrastructure.security.UserPrincipal;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationManagementController {

    private static final UUID SYSTEM_ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final IntegrationService integrationService;

    public IntegrationManagementController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IntegrationInstanceResponse>> createIntegration(
        @RequestBody CreateIntegrationRequest body,
        HttpServletRequest request
    ) {
        String connectionKey = body.connectionKey() != null ? body.connectionKey() : "default";
        SyncSchedule schedule = toSchedule(body.cadenceType().name(), body.cronExpression(), body.intervalMinutes(), body.timezone());
        IntegrationInstance instance = integrationService.createIntegrationInstance(
            body.tenantId(), body.channel(), connectionKey, schedule
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(toResponse(instance), requestId(request)));
    }

    @GetMapping
    public ApiResponse<List<IntegrationInstanceResponse>> listIntegrations(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<IntegrationInstanceResponse> list = integrationService.findAllByTenantId(tenantId)
            .stream().map(this::toResponse).toList();
        return ApiResponse.success(list, requestId(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<IntegrationInstanceResponse> getIntegration(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        return ApiResponse.success(toResponse(integrationService.findById(id)), requestId(request));
    }

    @PutMapping("/{id}/credentials")
    public ApiResponse<IntegrationInstanceResponse> attachCredential(
        @PathVariable UUID id,
        @RequestBody AttachCredentialRequest body,
        @AuthenticationPrincipal UserPrincipal principal,
        HttpServletRequest request
    ) {
        UUID actorId = principal != null ? principal.userId() : SYSTEM_ACTOR;
        IntegrationInstance instance = integrationService.attachCredential(
            id, new CredentialRef(body.credentialId()), actorId
        );
        return ApiResponse.success(toResponse(instance), requestId(request));
    }

    @DeleteMapping("/{id}/credentials")
    public ResponseEntity<Void> detachCredential(@PathVariable UUID id) {
        integrationService.detachCredential(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pause")
    public ApiResponse<IntegrationInstanceResponse> pause(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        return ApiResponse.success(toResponse(integrationService.pause(id)), requestId(request));
    }

    @PostMapping("/{id}/resume")
    public ApiResponse<IntegrationInstanceResponse> resume(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        return ApiResponse.success(toResponse(integrationService.resume(id)), requestId(request));
    }

    @PutMapping("/{id}/schedule")
    public ApiResponse<IntegrationInstanceResponse> updateSchedule(
        @PathVariable UUID id,
        @RequestBody UpdateScheduleRequest body,
        HttpServletRequest request
    ) {
        SyncSchedule schedule = toSchedule(body.cadenceType().name(), body.cronExpression(), body.intervalMinutes(), body.timezone());
        return ApiResponse.success(
            toResponse(integrationService.updateSyncSchedule(id, schedule)),
            requestId(request)
        );
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponse<IntegrationInstanceResponse>> runSyncNow(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        IntegrationInstance instance = integrationService.runSyncNow(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(toResponse(instance), requestId(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntegration(@PathVariable UUID id) {
        integrationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private SyncSchedule toSchedule(String cadenceTypeName, String cronExpression, Integer intervalMinutes, String timezone) {
        return switch (cadenceTypeName) {
            case "MANUAL" -> SyncSchedule.manual(timezone);
            case "CRON" -> SyncSchedule.cron(cronExpression, timezone);
            case "INTERVAL" -> SyncSchedule.interval(intervalMinutes, timezone);
            default -> throw new IllegalArgumentException("Unknown cadenceType: " + cadenceTypeName);
        };
    }

    private IntegrationInstanceResponse toResponse(IntegrationInstance instance) {
        UUID credentialId = instance.getCredentialRef() != null
            ? instance.getCredentialRef().credentialId()
            : null;
        var schedule = instance.getSyncSchedule();
        var health = instance.getHealth();
        return new IntegrationInstanceResponse(
            instance.getId(),
            instance.getTenantId(),
            instance.getChannel().name(),
            instance.getConnectionKey(),
            instance.getStatus().name(),
            credentialId,
            schedule.cadenceType().name(),
            schedule.cronExpression(),
            schedule.intervalMinutes(),
            schedule.timezone(),
            health.lastSyncAt(),
            health.lastSyncStatus().name(),
            health.lastSuccessAt(),
            health.lastErrorCode(),
            health.lastErrorMessage(),
            health.consecutiveFailures(),
            health.statusReason(),
            instance.getCredentialAttachedAt(),
            instance.getCreatedAt(),
            instance.getUpdatedAt()
        );
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? "unknown" : value.toString();
    }
}
