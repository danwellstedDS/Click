package com.derbysoft.click.modules.channelintegration.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.channelintegration.application.handlers.IntegrationService;
import com.derbysoft.click.modules.channelintegration.domain.IntegrationInstanceRepository;
import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.modules.tenantgovernance.api.ports.TenantGovernancePort;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceTest {

    @Mock
    private IntegrationInstanceRepository repository;

    @Mock
    private InProcessEventBus eventBus;

    @Mock
    private TenantGovernancePort tenantGovernancePort;

    @InjectMocks
    private IntegrationService integrationService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final SyncSchedule SCHEDULE = new SyncSchedule("0 * * * *", "UTC");

    @Test
    void shouldCallGovernancePortOnCreate() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.createIntegrationInstance(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);

        verify(tenantGovernancePort, times(1)).assertCanCreateIntegration(TENANT_ID);
    }

    @Test
    void shouldThrowForbiddenWhenGovernanceDenies() {
        doThrow(new DomainError.Forbidden("GOV_403", "Integration creation not permitted"))
            .when(tenantGovernancePort).assertCanCreateIntegration(any());

        assertThatThrownBy(() ->
            integrationService.createIntegrationInstance(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE)
        ).isInstanceOf(DomainError.Forbidden.class)
            .hasMessageContaining("not permitted");
    }

    @Test
    void shouldPublishEventsAfterCreate() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        integrationService.createIntegrationInstance(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);

        verify(eventBus, times(1)).publish(any());
    }

    @Test
    void shouldThrowNotFoundWhenIntegrationMissing() {
        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> integrationService.findById(missingId))
            .isInstanceOf(DomainError.NotFound.class)
            .hasMessageContaining(missingId.toString());
    }

    @Test
    void shouldSaveAndPublishOnPause() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(new com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef(UUID.randomUUID()));
        instance.clearEvents();

        when(repository.findById(instance.getId())).thenReturn(Optional.of(instance));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        integrationService.pause(instance.getId());

        verify(repository).save(any());
        verify(eventBus).publish(any());
    }
}
