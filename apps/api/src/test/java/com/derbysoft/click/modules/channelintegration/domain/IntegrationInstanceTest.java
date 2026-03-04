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
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncStatus;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IntegrationInstanceTest {

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final SyncSchedule SCHEDULE = SyncSchedule.cron("0 * * * *", "UTC");
    private static final CredentialRef CREDENTIAL = new CredentialRef(UUID.randomUUID());

    // ── existing lifecycle tests ────────────────────────────────────────────

    @Test
    void shouldCreateWithSetupRequiredStatus() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.SetupRequired.class);
        assertThat(instance.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(instance.getChannel()).isEqualTo(Channel.GOOGLE_ADS);
        assertThat(instance.getCredentialRef()).isNull();
    }

    @Test
    void shouldCaptureIntegrationCreatedEvent() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);

        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(IntegrationCreated.class);
        IntegrationCreated event = (IntegrationCreated) instance.getEvents().get(0);
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
        assertThat(event.channel()).isEqualTo(Channel.GOOGLE_ADS);
    }

    @Test
    void shouldTransitionToActiveWhenCredentialAttached() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.clearEvents();

        instance.attachCredential(CREDENTIAL, ACTOR_ID);

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        assertThat(instance.getCredentialRef()).isEqualTo(CREDENTIAL);
        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(CredentialAttached.class);
    }

    @Test
    void shouldThrowConflictWhenAttachingToAlreadyActive() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);

        assertThatThrownBy(() -> instance.attachCredential(new CredentialRef(UUID.randomUUID()), ACTOR_ID))
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("already Active");
    }

    @Test
    void shouldEmitIntegrationRecoveredWhenAttachingFromBroken() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
        instance.markBroken("test failure");
        instance.clearEvents();

        instance.attachCredential(new CredentialRef(UUID.randomUUID()), ACTOR_ID);

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        assertThat(instance.getEvents()).hasSize(2);
        assertThat(instance.getEvents().get(0)).isInstanceOf(CredentialAttached.class);
        assertThat(instance.getEvents().get(1)).isInstanceOf(IntegrationRecovered.class);
    }

    @Test
    void shouldTransitionToSetupRequiredWhenCredentialDetached() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
        instance.clearEvents();

        instance.detachCredential();

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.SetupRequired.class);
        assertThat(instance.getCredentialRef()).isNull();
    }

    @Test
    void shouldTransitionToPausedFromActive() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
        instance.clearEvents();

        instance.pause();

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Paused.class);
        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(IntegrationPaused.class);
    }

    @Test
    void shouldThrowWhenPausingNonActiveInstance() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);

        assertThatThrownBy(instance::pause)
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("Active");
    }

    @Test
    void shouldTransitionToActiveWhenResumed() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
        instance.pause();
        instance.clearEvents();

        instance.resume();

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        assertThat(instance.getEvents()).hasSize(1);
        assertThat(instance.getEvents().get(0)).isInstanceOf(IntegrationResumed.class);
    }

    @Test
    void shouldThrowWhenResumingNonPausedInstance() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);

        assertThatThrownBy(instance::resume)
            .isInstanceOf(DomainError.Conflict.class)
            .hasMessageContaining("Paused");
    }

    @Test
    void shouldThrowWhenRunSyncNowOnNonActiveInstance() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);

        assertThatThrownBy(instance::runSyncNow)
            .isInstanceOf(DomainError.ValidationError.class)
            .hasMessageContaining("Active");
    }

    @Test
    void shouldEmitSyncRequestedWhenRunSyncNow() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
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
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
        instance.markBroken("connection error");
        instance.clearEvents();

        instance.attachCredential(new CredentialRef(UUID.randomUUID()), ACTOR_ID);

        assertThat(instance.getStatus()).isInstanceOf(IntegrationStatus.Active.class);
        boolean hasRecovered = instance.getEvents().stream()
            .anyMatch(e -> e instanceof IntegrationRecovered);
        assertThat(hasRecovered).isTrue();
    }

    // ── new health & schedule tests ─────────────────────────────────────────

    @Test
    void shouldDefaultHealthToNeverOnCreate() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);

        assertThat(instance.getHealth()).isNotNull();
        assertThat(instance.getHealth().lastSyncStatus()).isEqualTo(SyncStatus.NEVER);
        assertThat(instance.getHealth().lastSyncAt()).isNull();
        assertThat(instance.getHealth().consecutiveFailures()).isEqualTo(0);
    }

    @Test
    void shouldRecordHealthAsSuccessAfterSyncSuccess() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);

        instance.recordSyncSuccess();

        assertThat(instance.getHealth().lastSyncStatus()).isEqualTo(SyncStatus.SUCCESS);
        assertThat(instance.getHealth().lastSyncAt()).isNotNull();
        assertThat(instance.getHealth().lastSuccessAt()).isNotNull();
        assertThat(instance.getHealth().lastSuccessAt()).isEqualTo(instance.getHealth().lastSyncAt());
        assertThat(instance.getHealth().consecutiveFailures()).isEqualTo(0);
        assertThat(instance.getHealth().lastErrorCode()).isNull();
        assertThat(instance.getHealth().lastErrorMessage()).isNull();
    }

    @Test
    void shouldIncrementConsecutiveFailuresOnMarkBroken() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);

        instance.markBroken("connection timeout");

        assertThat(instance.getHealth().lastSyncStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(instance.getHealth().consecutiveFailures()).isEqualTo(1);
        assertThat(instance.getHealth().lastErrorMessage()).isEqualTo("connection timeout");
        assertThat(instance.getHealth().lastSuccessAt()).isNull();
    }

    @Test
    void shouldPreserveLastSuccessAtAfterSubsequentFailure() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
        instance.recordSyncSuccess();
        Instant successTime = instance.getHealth().lastSuccessAt();

        // Now re-attach after broken to allow markBroken
        instance.markBroken("transient error");

        assertThat(instance.getHealth().lastSuccessAt()).isEqualTo(successTime);
    }

    @Test
    void shouldResetConsecutiveFailuresAfterSuccessFollowingFailure() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);
        instance.attachCredential(CREDENTIAL, ACTOR_ID);
        instance.markBroken("first failure");
        // re-attach to go back to Active
        instance.attachCredential(new CredentialRef(UUID.randomUUID()), ACTOR_ID);

        instance.recordSyncSuccess();

        assertThat(instance.getHealth().lastSyncStatus()).isEqualTo(SyncStatus.SUCCESS);
        assertThat(instance.getHealth().consecutiveFailures()).isEqualTo(0);
    }

    @Test
    void shouldSetCredentialAttachedAtOnAttachCredential() {
        IntegrationInstance instance = IntegrationInstance.create(TENANT_ID, Channel.GOOGLE_ADS, "default", SCHEDULE);

        assertThat(instance.getCredentialAttachedAt()).isNull();

        instance.attachCredential(CREDENTIAL, ACTOR_ID);

        assertThat(instance.getCredentialAttachedAt()).isNotNull();
        assertThat(instance.getUpdatedBy()).isEqualTo(ACTOR_ID);
    }

    @Test
    void shouldRejectCronScheduleWithoutExpression() {
        assertThatThrownBy(() -> new com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule(
            com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType.CRON,
            null, null, "UTC"
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cronExpression");
    }

    @Test
    void shouldRejectManualScheduleWithCronExpression() {
        assertThatThrownBy(() -> new com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule(
            com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType.MANUAL,
            "0 * * * *", null, "UTC"
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cronExpression");
    }

    @Test
    void shouldRejectIntervalScheduleWithoutMinutes() {
        assertThatThrownBy(() -> new com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule(
            com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType.INTERVAL,
            null, null, "UTC"
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("intervalMinutes");
    }

    @Test
    void shouldRejectIntervalScheduleBelowMinimum() {
        assertThatThrownBy(() -> SyncSchedule.interval(4, "UTC"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("intervalMinutes");
    }
}
