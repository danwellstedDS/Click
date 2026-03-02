package com.derbysoft.click.modules.channelintegration.application.handlers;

import com.derbysoft.click.modules.channelintegration.domain.IntegrationInstanceRepository;
import com.derbysoft.click.modules.channelintegration.domain.valueobjects.IntegrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Drives periodic sync for all Active integration instances.
 * Full cron evaluation is deferred to a later milestone — currently triggers
 * runSyncNow() for all Active instances on a 60-second fixed-delay schedule.
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
        repository.findAllByStatus(IntegrationStatus.ACTIVE).forEach(instance -> {
            try {
                integrationService.runSyncNow(instance.getId());
                log.debug("Scheduled sync triggered for integration {}", instance.getId());
            } catch (Exception e) {
                log.warn("Failed to trigger scheduled sync for integration {}: {}",
                    instance.getId(), e.getMessage());
            }
        });
    }
}
