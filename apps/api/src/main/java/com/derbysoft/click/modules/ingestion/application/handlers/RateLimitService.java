package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.bootstrap.messaging.InProcessEventBus;
import com.derbysoft.click.modules.ingestion.domain.events.ManualTriggerRateLimited;
import com.derbysoft.click.modules.ingestion.infrastructure.persistence.repository.SyncJobJpaRepository;
import com.derbysoft.click.sharedkernel.api.EventEnvelope;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private static final int MAX_MANUAL_TRIGGERS_PER_HOUR = 3;
    private static final List<String> MANUAL_TRIGGER_TYPES = List.of("MANUAL", "BACKFILL", "FORCE_RUN");

    private final SyncJobJpaRepository syncJobJpaRepository;
    private final InProcessEventBus eventBus;

    public RateLimitService(SyncJobJpaRepository syncJobJpaRepository, InProcessEventBus eventBus) {
        this.syncJobJpaRepository = syncJobJpaRepository;
        this.eventBus = eventBus;
    }

    public RateLimitResult checkAndRecord(UUID tenantId) {
        Instant since = Instant.now().minusSeconds(3600);
        long count = syncJobJpaRepository.countByTenantIdAndTriggerTypeInAndCreatedAtAfter(
            tenantId, MANUAL_TRIGGER_TYPES, since
        );

        if (count >= MAX_MANUAL_TRIGGERS_PER_HOUR) {
            long retryAfterSeconds = computeRetryAfterSeconds(tenantId, since);
            eventBus.publish(EventEnvelope.of("ManualTriggerRateLimited",
                new ManualTriggerRateLimited(tenantId, retryAfterSeconds, Instant.now())));
            return RateLimitResult.exceeded(retryAfterSeconds);
        }

        return RateLimitResult.ok();
    }

    private long computeRetryAfterSeconds(UUID tenantId, Instant since) {
        Instant oldest = syncJobJpaRepository.findOldestManualTriggerCreatedAt(
            tenantId, MANUAL_TRIGGER_TYPES, since);
        if (oldest == null) {
            return 60;
        }
        long secondsUntilExpiry = oldest.plusSeconds(3600).getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(1, secondsUntilExpiry);
    }

    public record RateLimitResult(boolean allowed, long retryAfterSeconds) {
        public static RateLimitResult ok() { return new RateLimitResult(true, 0); }
        public static RateLimitResult exceeded(long retryAfterSeconds) {
            return new RateLimitResult(false, retryAfterSeconds);
        }
    }
}
