package com.derbysoft.click.modules.channelintegration.application.handlers;

import com.derbysoft.click.modules.channelintegration.domain.IntegrationInstanceRepository;
import com.derbysoft.click.modules.channelintegration.domain.aggregates.IntegrationInstance;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.CadenceType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

/**
 * Drives periodic sync for Active integration instances with non-MANUAL cadence.
 * INTERVAL: triggers when now >= lastSyncAt + intervalMinutes (or never synced).
 * CRON: uses Spring CronExpression to evaluate whether the next run is due.
 */
@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final IntegrationInstanceRepository repository;
    private final IntegrationService integrationService;

    public ScheduleService(IntegrationInstanceRepository repository, IntegrationService integrationService) {
        this.repository = repository;
        this.integrationService = integrationService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void triggerScheduledSyncs() {
        repository.findAllSchedulable().forEach(instance -> {
            try {
                if (isDue(instance)) {
                    integrationService.runSyncNow(instance.getId());
                    log.debug("Scheduled sync triggered for integration {}", instance.getId());
                }
            } catch (Exception e) {
                log.warn("Failed to trigger scheduled sync for integration {}: {}",
                    instance.getId(), e.getMessage());
            }
        });
    }

    private boolean isDue(IntegrationInstance instance) {
        CadenceType cadenceType = instance.getSyncSchedule().cadenceType();
        return switch (cadenceType) {
            case MANUAL -> false;
            case INTERVAL -> isIntervalDue(instance);
            case CRON -> isCronDue(instance);
        };
    }

    private boolean isIntervalDue(IntegrationInstance instance) {
        Instant lastSyncAt = instance.getHealth().lastSyncAt();
        if (lastSyncAt == null) {
            return true;
        }
        int intervalMinutes = instance.getSyncSchedule().intervalMinutes();
        return Instant.now().isAfter(lastSyncAt.plusSeconds((long) intervalMinutes * 60));
    }

    private boolean isCronDue(IntegrationInstance instance) {
        Instant lastSyncAt = instance.getHealth().lastSyncAt();
        if (lastSyncAt == null) {
            return true;
        }
        String cronExpression = instance.getSyncSchedule().cronExpression();
        String timezone = instance.getSyncSchedule().timezone();
        try {
            CronExpression expr = CronExpression.parse(cronExpression);
            LocalDateTime lastCheck = lastSyncAt.atZone(ZoneId.of(timezone)).toLocalDateTime();
            LocalDateTime nextRun = expr.next(lastCheck);
            return nextRun == null || !nextRun.isAfter(LocalDateTime.now(ZoneId.of(timezone)));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cron expression '{}' for integration {}: {}",
                cronExpression, instance.getId(), e.getMessage());
            return false;
        }
    }
}
