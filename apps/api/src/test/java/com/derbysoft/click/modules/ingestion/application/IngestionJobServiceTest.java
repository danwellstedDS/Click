package com.derbysoft.click.modules.ingestion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.AccountBindingInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import com.derbysoft.click.modules.ingestion.application.handlers.IngestionJobService;
import com.derbysoft.click.modules.ingestion.application.handlers.RateLimitService;
import com.derbysoft.click.modules.ingestion.application.handlers.RateLimitService.RateLimitResult;
import com.derbysoft.click.modules.ingestion.domain.SyncIncidentRepository;
import com.derbysoft.click.modules.ingestion.domain.SyncJobRepository;
import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.SyncJobStatus;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.TriggerType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IngestionJobServiceTest {

    @Mock SyncJobRepository syncJobRepository;
    @Mock SyncIncidentRepository incidentRepository;
    @Mock RateLimitService rateLimitService;
    @Mock GoogleAdsQueryPort googleAdsQueryPort;
    @Mock InProcessEventBus eventBus;

    private IngestionJobService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID INTEGRATION_ID = UUID.randomUUID();
    private static final UUID BINDING_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new IngestionJobService(syncJobRepository, incidentRepository,
            rateLimitService, googleAdsQueryPort, eventBus);
    }

    private AccountBindingInfo activeBinding() {
        return new AccountBindingInfo(BINDING_ID, TENANT_ID, "123-456-7890", "ACTIVE", "STANDARD", null, null);
    }

    private AccountBindingInfo staleBinding() {
        return new AccountBindingInfo(UUID.randomUUID(), TENANT_ID, "999-000-0001", "STALE", "STANDARD", null, null);
    }

    private AccountBindingInfo brokenBinding() {
        return new AccountBindingInfo(UUID.randomUUID(), TENANT_ID, "999-000-0002", "BROKEN", "STANDARD", null, null);
    }

    private SyncJob savedJob(SyncJob job) {
        return job; // pass-through mock
    }

    @Test
    void shouldEnqueueDailySyncForEachActiveBinding() {
        when(googleAdsQueryPort.listActiveBindings(TENANT_ID))
            .thenReturn(List.of(activeBinding(), staleBinding()));
        when(syncJobRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.enqueueDailySync(INTEGRATION_ID, TENANT_ID);

        verify(syncJobRepository, times(2)).save(any(SyncJob.class));
    }

    @Test
    void shouldSkipBrokenAndRemovedBindings() {
        when(googleAdsQueryPort.listActiveBindings(TENANT_ID))
            .thenReturn(List.of(brokenBinding(),
                new AccountBindingInfo(UUID.randomUUID(), TENANT_ID, "removed-1", "REMOVED", "STANDARD", null, null)));

        service.enqueueDailySync(INTEGRATION_ID, TENANT_ID);

        verify(syncJobRepository, never()).save(any());
    }

    @Test
    void shouldIncludeStaleBindings() {
        when(googleAdsQueryPort.listActiveBindings(TENANT_ID))
            .thenReturn(List.of(staleBinding()));
        when(syncJobRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(syncJobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.enqueueDailySync(INTEGRATION_ID, TENANT_ID);

        verify(syncJobRepository, times(1)).save(any(SyncJob.class));
    }

    @Test
    void shouldRejectManualSyncWhenRateLimitExceeded() {
        when(rateLimitService.checkAndRecord(TENANT_ID))
            .thenReturn(RateLimitResult.exceeded(1800));

        assertThatThrownBy(() ->
            service.enqueueManualSync(TENANT_ID, INTEGRATION_ID, "123-456-7890", "test", "user"))
            .isInstanceOf(DomainError.ValidationError.class)
            .hasMessageContaining("Rate limit");
    }

    @Test
    void shouldRejectBackfillBeyond14Days() {
        DateWindow tooLong = new DateWindow(
            LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 20));

        assertThatThrownBy(() ->
            service.enqueueBackfill(TENANT_ID, INTEGRATION_ID, "123-456-7890",
                tooLong, "big backfill", "user"))
            .isInstanceOf(DomainError.ValidationError.class)
            .hasMessageContaining("14 days");
    }

    @Test
    void shouldThrowWhenForceRunRateLimited() {
        when(rateLimitService.checkAndRecord(TENANT_ID))
            .thenReturn(RateLimitResult.exceeded(600));

        assertThatThrownBy(() ->
            service.forceRun(TENANT_ID, INTEGRATION_ID, "123-456-7890", "force", "user"))
            .isInstanceOf(DomainError.ValidationError.class)
            .hasMessageContaining("Rate limit");
    }
}
