package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.controller;

import com.derbysoft.click.modules.googleadsmanagement.application.handlers.AccountBindingService;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.AccountBinding;
import com.derbysoft.click.modules.googleadsmanagement.domain.valueobjects.BindingType;
import com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto.BindingResponse;
import com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto.CreateBindingRequest;
import com.derbysoft.click.sharedkernel.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/google/bindings")
public class AccountBindingController {

    private final AccountBindingService bindingService;

    public AccountBindingController(AccountBindingService bindingService) {
        this.bindingService = bindingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BindingResponse>> createBinding(
        @RequestBody CreateBindingRequest body,
        HttpServletRequest request
    ) {
        BindingType bindingType = BindingType.valueOf(body.bindingType());
        AccountBinding binding = bindingService.createBinding(
            body.connectionId(), body.customerId(), bindingType
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(toResponse(binding), requestId(request)));
    }

    @GetMapping
    public ApiResponse<List<BindingResponse>> listByConnection(
        @RequestParam UUID connectionId,
        HttpServletRequest request
    ) {
        List<BindingResponse> bindings = bindingService.findByConnectionId(connectionId).stream()
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(bindings, requestId(request));
    }

    @GetMapping("/resolve")
    public ApiResponse<List<BindingResponse>> resolveActiveBindings(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        List<BindingResponse> bindings = bindingService.resolveApplicableAccounts(tenantId).stream()
            .map(this::toResponse)
            .toList();
        return ApiResponse.success(bindings, requestId(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeBinding(@PathVariable UUID id) {
        bindingService.removeBinding(id);
        return ResponseEntity.noContent().build();
    }

    private BindingResponse toResponse(AccountBinding binding) {
        return new BindingResponse(
            binding.getId(),
            binding.getConnectionId(),
            binding.getTenantId(),
            binding.getCustomerId(),
            binding.getStatus().name(),
            binding.getBindingType().name(),
            binding.getCreatedAt()
        );
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? "unknown" : value.toString();
    }
}
