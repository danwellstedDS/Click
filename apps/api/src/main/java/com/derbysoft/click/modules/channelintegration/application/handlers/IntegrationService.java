package com.derbysoft.click.modules.channelintegration.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.channelintegration.domain.IntegrationInstanceRepository;
import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.modules.tenantgovernance.api.ports.TenantGovernancePort;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationService {

    private final IntegrationInstanceRepository repository;
    private final InProcessEventBus eventBus;
    private final TenantGovernancePort tenantGovernancePort;

    public IntegrationService(
        IntegrationInstanceRepository repository,
        InProcessEventBus eventBus,
        TenantGovernancePort tenantGovernancePort
    ) {
        this.repository = repository;
        this.eventBus = eventBus;
        this.tenantGovernancePort = tenantGovernancePort;
    }

    @Transactional
    public IntegrationInstance createIntegrationInstance(
        UUID tenantId, Channel channel, String connectionKey, SyncSchedule schedule
    ) {
        tenantGovernancePort.assertCanCreateIntegration(tenantId);
        IntegrationInstance instance = IntegrationInstance.create(tenantId, channel, connectionKey, schedule);
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance attachCredential(UUID id, CredentialRef credentialRef, UUID actorId) {
        IntegrationInstance instance = requireById(id);
        instance.attachCredential(credentialRef, actorId);
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance detachCredential(UUID id) {
        IntegrationInstance instance = requireById(id);
        instance.detachCredential();
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance pause(UUID id) {
        IntegrationInstance instance = requireById(id);
        instance.pause();
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance resume(UUID id) {
        IntegrationInstance instance = requireById(id);
        instance.resume();
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance updateSyncSchedule(UUID id, SyncSchedule schedule) {
        IntegrationInstance instance = requireById(id);
        instance.updateSchedule(schedule);
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance runSyncNow(UUID id) {
        IntegrationInstance instance = requireById(id);
        instance.runSyncNow();
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance markBroken(UUID id, String reason) {
        IntegrationInstance instance = requireById(id);
        instance.markBroken(reason);
        IntegrationInstance saved = repository.save(instance);
        publishAndClear(saved);
        return saved;
    }

    @Transactional
    public IntegrationInstance recordSyncSuccess(UUID id) {
        IntegrationInstance instance = requireById(id);
        instance.recordSyncSuccess();
        return repository.save(instance);
    }

    public IntegrationInstance findById(UUID id) {
        return requireById(id);
    }

    public List<IntegrationInstance> findAllByTenantId(UUID tenantId) {
        return repository.findAllByTenantId(tenantId);
    }

    @Transactional
    public void deleteById(UUID id) {
        requireById(id);
        repository.deleteById(id);
    }

    private IntegrationInstance requireById(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new DomainError.NotFound("INT_404",
                "IntegrationInstance not found: " + id));
    }

    private void publishAndClear(IntegrationInstance instance) {
        instance.getEvents().forEach(event ->
            eventBus.publish(EventEnvelope.of(event.getClass().getSimpleName(), event))
        );
        instance.clearEvents();
    }
}
