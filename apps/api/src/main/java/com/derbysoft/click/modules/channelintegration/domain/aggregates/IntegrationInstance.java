package com.derbysoft.click.modules.channelintegration.domain.aggregates;

import com.derbysoft.click.modules.channelintegration.domain.events.CredentialAttached;
import com.derbysoft.click.modules.channelintegration.domain.events.CredentialDetached;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationCreated;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationMarkedBroken;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationPaused;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationRecovered;
import com.derbysoft.click.modules.channelintegration.domain.events.IntegrationResumed;
import com.derbysoft.click.modules.channelintegration.domain.events.SyncRequested;
import com.derbysoft.click.modules.channelintegration.domain.events.SyncScheduleUpdated;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.Channel;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CredentialRef;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationHealth;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationStatus;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.SyncSchedule;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class IntegrationInstance {

    private final UUID id;
    private final UUID tenantId;
    private final Channel channel;
    private IntegrationStatus status;
    private CredentialRef credentialRef;
    private SyncSchedule syncSchedule;
    private IntegrationHealth health;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Object> events = new ArrayList<>();

    private IntegrationInstance(
        UUID id,
        UUID tenantId,
        Channel channel,
        IntegrationStatus status,
        CredentialRef credentialRef,
        SyncSchedule syncSchedule,
        IntegrationHealth health,
        Instant createdAt,
        Instant updatedAt
    ) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        this.id = id;
        this.tenantId = tenantId;
        this.channel = channel;
        this.status = status;
        this.credentialRef = credentialRef;
        this.syncSchedule = syncSchedule;
        this.health = health;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Reconstitutes an existing aggregate from persistence (no events emitted).
     */
    public static IntegrationInstance reconstitute(
        UUID id,
        UUID tenantId,
        Channel channel,
        IntegrationStatus status,
        CredentialRef credentialRef,
        SyncSchedule syncSchedule,
        IntegrationHealth health,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new IntegrationInstance(id, tenantId, channel, status, credentialRef,
            syncSchedule, health, createdAt, updatedAt);
    }

    /**
     * Factory method — creates a new integration instance in SetupRequired state.
     */
    public static IntegrationInstance create(UUID tenantId, Channel channel, SyncSchedule syncSchedule) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(syncSchedule, "syncSchedule must not be null");

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        IntegrationInstance instance = new IntegrationInstance(
            id, tenantId, channel,
            IntegrationStatus.SETUP_REQUIRED,
            null, syncSchedule, null,
            now, now
        );
        instance.events.add(new IntegrationCreated(id, tenantId, channel, now));
        return instance;
    }

    /**
     * Attaches a credential reference. Transitions SetupRequired → Active or Broken → Active.
     * Emits CredentialAttached and optionally IntegrationRecovered.
     */
    public void attachCredential(CredentialRef ref) {
        Objects.requireNonNull(ref, "credentialRef must not be null");
        if (status instanceof IntegrationStatus.Active) {
            throw new DomainError.Conflict("INT_001",
                "Cannot attach credential: integration is already Active");
        }
        boolean wasBreaking = status instanceof IntegrationStatus.Broken;
        this.credentialRef = ref;
        this.status = IntegrationStatus.ACTIVE;
        this.updatedAt = Instant.now();
        events.add(new CredentialAttached(id, ref, updatedAt));
        if (wasBreaking) {
            events.add(new IntegrationRecovered(id, updatedAt));
        }
    }

    /**
     * Detaches credential. Only permitted when Active. Transitions Active → SetupRequired.
     */
    public void detachCredential() {
        requireStatus(IntegrationStatus.Active.class, "detachCredential");
        this.credentialRef = null;
        this.status = IntegrationStatus.SETUP_REQUIRED;
        this.updatedAt = Instant.now();
        events.add(new CredentialDetached(id, updatedAt));
    }

    /**
     * Pauses the integration. Only permitted when Active. Transitions Active → Paused.
     */
    public void pause() {
        requireStatus(IntegrationStatus.Active.class, "pause");
        this.status = IntegrationStatus.PAUSED;
        this.updatedAt = Instant.now();
        events.add(new IntegrationPaused(id, updatedAt));
    }

    /**
     * Resumes the integration. Only permitted when Paused. Transitions Paused → Active.
     */
    public void resume() {
        requireStatus(IntegrationStatus.Paused.class, "resume");
        this.status = IntegrationStatus.ACTIVE;
        this.updatedAt = Instant.now();
        events.add(new IntegrationResumed(id, updatedAt));
    }

    /**
     * Updates the sync schedule. Permitted in any status.
     */
    public void updateSchedule(SyncSchedule newSchedule) {
        Objects.requireNonNull(newSchedule, "syncSchedule must not be null");
        this.syncSchedule = newSchedule;
        this.updatedAt = Instant.now();
        events.add(new SyncScheduleUpdated(id, newSchedule, updatedAt));
    }

    /**
     * Triggers an immediate sync. Only permitted when Active.
     * Emits SyncRequested with a fresh syncRunId.
     */
    public void runSyncNow() {
        if (!(status instanceof IntegrationStatus.Active)) {
            throw new DomainError.ValidationError("INT_003",
                "runSyncNow is only permitted when status is Active; current status: " + status.name());
        }
        if (credentialRef == null) {
            throw new DomainError.ValidationError("INT_004",
                "Cannot run sync: no credential attached");
        }
        this.updatedAt = Instant.now();
        events.add(new SyncRequested(id, tenantId, channel, credentialRef,
            UUID.randomUUID(), updatedAt));
    }

    /**
     * Marks the integration as Broken. Only permitted when Active.
     */
    public void markBroken(String reason) {
        requireStatus(IntegrationStatus.Active.class, "markBroken");
        this.status = IntegrationStatus.BROKEN;
        this.health = new IntegrationHealth(Instant.now(), reason);
        this.updatedAt = Instant.now();
        events.add(new IntegrationMarkedBroken(id, reason, updatedAt));
    }

    /**
     * Updates health after a successful sync (no status change, no event emitted).
     */
    public void recordSyncSuccess() {
        this.health = new IntegrationHealth(Instant.now(), null);
        this.updatedAt = Instant.now();
    }

    private <T extends IntegrationStatus> void requireStatus(Class<T> expectedType, String operation) {
        if (!expectedType.isInstance(status)) {
            throw new DomainError.Conflict("INT_002",
                "Cannot " + operation + ": expected status " + expectedType.getSimpleName()
                + " but was " + status.name());
        }
    }

    public List<Object> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void clearEvents() {
        events.clear();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public Channel getChannel() { return channel; }
    public IntegrationStatus getStatus() { return status; }
    public CredentialRef getCredentialRef() { return credentialRef; }
    public SyncSchedule getSyncSchedule() { return syncSchedule; }
    public IntegrationHealth getHealth() { return health; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
