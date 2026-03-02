package com.derbysoft.click.modules.channelintegration.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.events.CredentialAttached;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationCreated;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationPaused;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationRecovered;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationResumed;
import com.derbysoft.click.modules.channelintegration.domain.events.SyncRequested;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationStatus;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IntegrationInstanceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final SyncSchedule SCHEDULE = new SyncSchedule("0 * * * *", "UTC");
    private static final CredentialRef CREDENTIAL = new CredentialRef(UUID.randomUUID());

    @Test
    void shouldCreateWithSetupRequiredStatus() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.SetupRequired.class);
        assertThat(instance.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(instance.getChannel()).isEqualTo(Channel.GOOGLE_ADS);
        assertThat(instance.getCredentialRef()).isNull();
    }

    @Test
    void shouldCaptureIntegrationCreatedEvent() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);

        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(IntegrationCreated.class);
        IntegrationCreated event = (IntegrationCreated) instance.getEvents().get(0);
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
        assertThat(event.channel()).isEqualTo(Channel.GOOGLE_ADS);
    }

    @Test
    void shouldTransitionToActiveWhenCredentialAttached() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.clearEvents();

        instance.attachCredential(CREDENTIAL);

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        assertThat(instance.getCredentialRef()).isEqualTo(CREDENTIAL);
        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(CredentialAttached.class);
    }

    @Test
    void shouldThrowConflictWhenAttachingToAlreadyActive() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);

        assertThatThrownBy(() -> instance.attachCredential(new CredentialRef(UUID.randomUUID())))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("already Active");
    }

    @Test
    void shouldEmitIntegrationRecoveredWhenAttachingFromBroken() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);
        instance.markBroken("test failure");
        instance.clearEvents();

        instance.attachCredential(new CredentialRef(UUID.randomUUID()));

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        assertThat(instance.getEvents()).hasSize(2);
        assertThat(instance.getEvents().get(0)).isInstanceOf(CredentialAttached.class);
        assertThat(instance.getEvents().get(1)).isInstanceOf(IntegrationRecovered.class);
    }

    @Test
    void shouldTransitionToSetupRequiredWhenCredentialDetached() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);
        instance.clearEvents();

        instance.detachCredential();

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.SetupRequired.class);
        assertThat(instance.getCredentialRef()).isNull();
    }

    @Test
    void shouldTransitionToPausedFromActive() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);
        instance.clearEvents();

        instance.pause();

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Paused.class);
        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(IntegrationPaused.class);
    }

    @Test
    void shouldThrowWhenPausingNonActiveInstance() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        // status = SetupRequired

        assertThatThrownBy(instance::pause)
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("Active");
    }

    @Test
    void shouldTransitionToActiveWhenResumed() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);
        instance.pause();
        instance.clearEvents();

        instance.resume();

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(IntegrationResumed.class);
    }

    @Test
    void shouldThrowWhenResumingNonPausedInstance() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);
        // status = Active, not Paused

        assertThatThrownBy(instance::resume)
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("Paused");
    }

    @Test
    void shouldThrowWhenRunSyncNowOnNonActiveInstance() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        // status = SetupRequired

        assertThatThrownBy(instance::runSyncNow)
            .isInstanceOf(DomainError.ValidationError.class)
            .hasMessageContaining("Active");
    }

    @Test
    void shouldEmitSyncRequestedWhenRunSyncNow() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);
        instance.clearEvents();

        instance.runSyncNow();

        assertThat(instance.getEvents()).hasSize(1);
        SyncRequested event = (SyncRequested) instance.getEvents().get(0);
        assertThat(event.integrationId()).isEqualTo(instance.getId());
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
        assertThat(event.channel()).isEqualTo(Channel.GOOGLE_ADS);
        assertThat(event.credentialRef()).isEqualTo(CREDENTIAL);
        assertThat(event.syncRunId()).isNotNull();
    }

    @Test
    void shouldTransitionBrokenToActiveOnCredentialReattach() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, SCHEDULE);
        instance.attachCredential(CREDENTIAL);
        instance.markBroken("connection error");
        instance.clearEvents();

        instance.attachCredential(new CredentialRef(UUID.randomUUID()));

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        boolean hasRecovered = instance.getEvents().stream()
            .anyMatch(e -> e instanceof IntegrationRecovered);
        assertThat(hasRecovered).isTrue();
    }
}
