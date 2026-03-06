package com.derbysoft.click.modules.campaignexecution.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.campaignexecution.application.handlers.ExecutionIncidentLifecycleService;
import com.derbysoft.click.modules.campaignexecution.domain.ExecutionIncidentRepository;
import com.derbysoft.click.modules.campaignexecution.domain.aggregates.ExecutionIncident;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.IncidentStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionIncidentLifecycleServiceTest {

    @Mock ExecutionIncidentRepository incidentRepository;
    @Mock InProcessEventBus eventBus;

    private ExecutionIncidentLifecycleService service;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String KEY = "revision:item:CREATE_CAMPAIGN:0";
    private static final Instant NOW = Instant.parse("2026-03-06T09:00:00Z");

    @BeforeEach
    void setUp() {
        service = new ExecutionIncidentLifecycleService(incidentRepository, eventBus);
    }

    @Test
    void shouldOpenNewIncidentOnFirstFailure() {
        when(incidentRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.onFailure(KEY, TENANT_ID, FailureClass.TRANSIENT);

        verify(incidentRepository).save(any(ExecutionIncident.class));
    }

    @Test
    void shouldIncrementExistingIncidentOnSubsequentFailure() {
        ExecutionIncident existing = ExecutionIncident.open(
            UUID.randomUUID(), KEY, TENANT_ID, FailureClass.TRANSIENT, NOW);
        when(incidentRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.onFailure(KEY, TENANT_ID, FailureClass.TRANSIENT);

        assertThat(existing.getConsecutiveFailures()).isEqualTo(2);
        verify(incidentRepository).save(existing);
    }

    @Test
    void shouldAutoCloseIncidentOnSuccess() {
        ExecutionIncident existing = ExecutionIncident.open(
            UUID.randomUUID(), KEY, TENANT_ID, FailureClass.TRANSIENT, NOW);
        when(incidentRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.onSuccess(KEY, TENANT_ID);

        assertThat(existing.getStatus()).isEqualTo(IncidentStatus.AUTO_CLOSED);
        verify(incidentRepository).save(existing);
    }

    @Test
    void shouldDoNothingOnSuccessWhenNoIncidentExists() {
        when(incidentRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());

        service.onSuccess(KEY, TENANT_ID);

        verify(incidentRepository, never()).save(any());
    }
}
