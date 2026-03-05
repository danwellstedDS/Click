package com.derbysoft.click.modules.googleadsmanagement.interfaces.http.controller;

import com.derbysoft.click.modules.googleadsmanagement.application.handlers.DiscoverAccountsHandler;
import com.derbysoft.click.modules.googleadsmanagement.application.handlers.GoogleConnectionService;
import com.derbysoft.click.modules.googleadsmanagement.application.ports.GoogleAdsApiPort;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.modules.googleadsmanagement.domain.entities.AccountGraphState;
import com.derbysoft.click.modules.googleadsmanagement.infrastructure.persistence.repository.AccountGraphStateRepository;
import com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto.AccountGraphStateResponse;
import com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto.ConnectionResponse;
import com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto.CreateConnectionRequest;
import com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto.RotateCredentialRequest;
import com.derbysoft.click.modules.googleadsmanagement.interfaces.http.dto.ValidationResponse;
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
@RequestMapping("/api/v1/google/connections")
public class GoogleConnectionController {

    private final GoogleConnectionService connectionService;
    private final DiscoverAccountsHandler discoverAccountsHandler;
    private final AccountGraphStateRepository graphStateRepository;
    private final GoogleAdsApiPort googleAdsApiPort;

    public GoogleConnectionController(
        GoogleConnectionService connectionService,
        DiscoverAccountsHandler discoverAccountsHandler,
        AccountGraphStateRepository graphStateRepository,
        GoogleAdsApiPort googleAdsApiPort
    ) {
        this.connectionService = connectionService;
        this.discoverAccountsHandler = discoverAccountsHandler;
        this.graphStateRepository = graphStateRepository;
        this.googleAdsApiPort = googleAdsApiPort;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConnectionResponse>> createConnection(
        @RequestBody CreateConnectionRequest body,
        HttpServletRequest request
    ) {
        GoogleConnection connection = connectionService.createConnection(
            body.tenantId(), body.managerId(), body.credentialPath()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(toResponse(connection), requestId(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ConnectionResponse>> getConnectionByTenant(
        @RequestParam UUID tenantId,
        HttpServletRequest request
    ) {
        return connectionService.findByTenantId(tenantId)
            .map(conn -> ResponseEntity.ok(
                ApiResponse.success(toResponse(conn), requestId(request))
            ))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/rotate-credential")
    public ApiResponse<ConnectionResponse> rotateCredential(
        @PathVariable UUID id,
        @RequestBody RotateCredentialRequest body,
        HttpServletRequest request
    ) {
        GoogleConnection connection = connectionService.rotateCredential(id, body.newCredentialPath());
        return ApiResponse.success(toResponse(connection), requestId(request));
    }

    @PostMapping("/{id}/discover")
    public ResponseEntity<ApiResponse<ConnectionResponse>> triggerDiscovery(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        discoverAccountsHandler.discover(id);
        GoogleConnection connection = connectionService.findById(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(toResponse(connection), requestId(request)));
    }

    @PostMapping("/{id}/validate")
    public ApiResponse<ValidationResponse> validateConnection(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        GoogleConnection connection = connectionService.findById(id);
        boolean healthy = googleAdsApiPort.validateCredential(
            connection.getManagerId(), connection.getCredentialPath()
        );
        return ApiResponse.success(
            new ValidationResponse(healthy, connection.getManagerId(), connection.getStatus().name()),
            requestId(request)
        );
    }

    @GetMapping("/{id}/accounts")
    public ApiResponse<List<AccountGraphStateResponse>> getAccountGraph(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        List<AccountGraphStateResponse> accounts = graphStateRepository.findByConnectionId(id).stream()
            .map(this::toGraphResponse)
            .toList();
        return ApiResponse.success(accounts, requestId(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(@PathVariable UUID id) {
        connectionService.findById(id);
        return ResponseEntity.noContent().build();
    }

    private ConnectionResponse toResponse(GoogleConnection conn) {
        return new ConnectionResponse(
            conn.getId(),
            conn.getTenantId(),
            conn.getManagerId(),
            conn.getStatus().name(),
            conn.getLastDiscoveredAt(),
            conn.getCreatedAt()
        );
    }

    private AccountGraphStateResponse toGraphResponse(AccountGraphState state) {
        return new AccountGraphStateResponse(
            state.getCustomerId(),
            state.getAccountName(),
            state.getCurrencyCode(),
            state.getTimeZone(),
            state.getDiscoveredAt()
        );
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute("requestId");
        return value == null ? "unknown" : value.toString();
    }
}
