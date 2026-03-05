package com.derbysoft.click.modules.googleadsmanagement.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.domain.GoogleConnectionRepository;
import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleConnectionService {

    private final GoogleConnectionRepository repository;
    private final InProcessEventBus eventBus;

    public GoogleConnectionService(GoogleConnectionRepository repository, InProcessEventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    @Transactional
    public GoogleConnection createConnection(UUID tenantId, String managerId, String credentialPath) {
        if (tenantId == null) {
            throw new DomainError.ValidationError("GCONN_100", "tenantId must not be null");
        }
        repository.findByTenantId(tenantId).ifPresent(existing -> {
            throw new DomainError.Conflict("GCONN_101",
                "A Google connection already exists for tenant: " + tenantId);
        });
        UUID id = UUID.randomUUID();
        GoogleConnection connection = GoogleConnection.create(id, tenantId, managerId, credentialPath, Instant.now());
        GoogleConnection saved = repository.save(connection);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public GoogleConnection rotateCredential(UUID connectionId, String newCredentialPath) {
        GoogleConnection connection = findById(connectionId);
        connection.rotateCredential(newCredentialPath);
        GoogleConnection saved = repository.save(connection);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public GoogleConnection markBroken(UUID connectionId, String reason) {
        GoogleConnection connection = findById(connectionId);
        connection.markBroken(reason);
        GoogleConnection saved = repository.save(connection);
        publishAndClear(saved);
        return saved;
    }

    public GoogleConnection findById(UUID connectionId) {
        return repository.findById(connectionId)
            .orElseThrow(() -> new DomainError.NotFound("GCONN_404",
                "GoogleConnection not found: " + connectionId));
    }

    public Optional<GoogleConnection> findByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId);
    }

    public List<GoogleConnection> findAllActive() {
        return repository.findAllActive();
    }

    private void publishAndClear(GoogleConnection connection) {
        connection.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        connection.clearEvents();
    }
}
