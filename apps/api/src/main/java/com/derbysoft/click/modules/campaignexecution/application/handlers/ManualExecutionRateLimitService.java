package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.domain.WriteActionRepository;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.TriggerType;
import com.derbysoft.click.sharedkernel.domain.errors.DomainError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ManualExecutionRateLimitService {

    private static final int MAX_MANUAL_TRIGGERS_PER_HOUR = 3;
    private static final List<TriggerType> MANUAL_TRIGGER_TYPES =
        List.of(TriggerType.FORCE_RUN, TriggerType.RETRY);

    private final WriteActionRepository writeActionRepository;

    public ManualExecutionRateLimitService(WriteActionRepository writeActionRepository) {
        this.writeActionRepository = writeActionRepository;
    }

    public void checkOrThrow(UUID tenantId) {
        Instant since = Instant.now().minusSeconds(3600);
        long count = writeActionRepository.countManualTriggersSince(tenantId, MANUAL_TRIGGER_TYPES, since);
        if (count >= MAX_MANUAL_TRIGGERS_PER_HOUR) {
            throw new DomainError.Conflict("CE_429",
                "Manual execution rate limit exceeded (max " + MAX_MANUAL_TRIGGERS_PER_HOUR +
                " per hour). Try again later.");
        }
    }
}
