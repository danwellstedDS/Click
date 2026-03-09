package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.errors.RateLimitExceededException;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ManualExecutionRateLimitService {

    private static final int MAX_MANUAL_TRIGGERS_PER_HOUR = 3;
    // Gap #6: APPLY is now counted as a manual trigger alongside FORCE_RUN and RETRY
    private static final List<TriggerType> MANUAL_TRIGGER_TYPES =
        List.of(TriggerType.FORCE_RUN, TriggerType.RETRY, TriggerType.APPLY);

    private final WriteActionRepository writeActionRepository;

    public ManualExecutionRateLimitService(WriteActionRepository writeActionRepository) {
        this.writeActionRepository = writeActionRepository;
    }

    /**
     * Checks whether the tenant has exceeded the manual trigger rate limit.
     *
     * @throws RateLimitExceededException with retryAfter duration if the limit is exceeded
     */
    public void checkOrThrow(UUID tenantId) {
        Instant since = Instant.now().minusSeconds(3600);
        long count = writeActionRepository.countManualTriggersSince(tenantId, MANUAL_TRIGGER_TYPES, since);
        if (count >= MAX_MANUAL_TRIGGERS_PER_HOUR) {
            // Gap #7: surface retryAfter so callers can return 429 with Retry-After header
            throw new RateLimitExceededException(Duration.ofHours(1));
        }
    }
}
