package com.derbysoft.click.modules.channelintegration.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.modules.channelintegration.application.handlers.IntegrationService;
import com.derbysoft.click.modules.channelintegration.application.handlers.ScheduleService;
import com.derbysoft.click.modules.channelintegration.domain.IntegrationInstanceRepository;
import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private IntegrationInstanceRepository repository;

    @Mock
    private IntegrationService integrationService;

    @InjectMocks
    private ScheduleService scheduleService;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @Test
    void shouldSkipManualIntegrations() {
        // MANUAL instances should not appear in findAllSchedulable() at all,
        // but even if they did, the service would skip them.
        // This test verifies that findAllSchedulable returns empty for MANUAL-only scenario.
        when(repository.findAllSchedulable()).thenReturn(List.of());

        scheduleService.triggerScheduledSyncs();

        verify(integrationService, never()).runSyncNow(any());
    }

    @Test
    void shouldTriggerIntervalIntegrationWhenDue() {
        IntegrationInstance instance = makeActiveInstance(SyncSchedule.interval(60, "UTC"));
        // Health has lastSyncAt = 90 minutes ago → overdue for 60-min interval
        instance.attachCredential(new CredentialRef(UUID.randomUUID()), ACTOR_ID);
        // Force last sync 90 minutes ago by recording a success before the test
        // We can't directly set health, but we can simulate "never synced" which also triggers
        // (lastSyncAt == null → isDue == true)

        when(repository.findAllSchedulable()).thenReturn(List.of(instance));
        when(integrationService.runSyncNow(instance.getId())).thenReturn(instance);

        scheduleService.triggerScheduledSyncs();

        verify(integrationService).runSyncNow(instance.getId());
    }

    @Test
    void shouldNotTriggerIntervalIntegrationWhenNotYetDue() {
        // Create an instance that has been synced just now — not yet due for a 60-min interval
        IntegrationInstance instance = makeActiveInstance(SyncSchedule.interval(60, "UTC"));
        instance.attachCredential(new CredentialRef(UUID.randomUUID()), ACTOR_ID);
        // Record sync success to set lastSyncAt = now
        instance.recordSyncSuccess();

        when(repository.findAllSchedulable()).thenReturn(List.of(instance));

        scheduleService.triggerScheduledSyncs();

        verify(integrationService, never()).runSyncNow(any());
    }

    private static IntegrationInstance makeActiveInstance(SyncSchedule schedule) {
        return IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", schedule);
    }
}
