package com.derbysoft.click.modules.googleadsmanagement.application.handlers;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryScheduleService {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryScheduleService.class);

    private final GoogleConnectionService googleConnectionService;
    private final DiscoverAccountsHandler discoverAccountsHandler;

    public DiscoveryScheduleService(
        GoogleConnectionService googleConnectionService,
        DiscoverAccountsHandler discoverAccountsHandler
    ) {
        this.googleConnectionService = googleConnectionService;
        this.discoverAccountsHandler = discoverAccountsHandler;
    }

    @Scheduled(fixedDelay = 60_000)
    public void triggerScheduledDiscovery() {
        googleConnectionService.findAllActive().forEach(conn -> {
            boolean isDue = conn.getLastDiscoveredAt() == null
                || conn.getLastDiscoveredAt().plusSeconds(3600).isBefore(Instant.now());
            if (isDue) {
                try {
                    discoverAccountsHandler.discover(conn.getId());
                    log.debug("Scheduled discovery triggered for connection {}", conn.getId());
                } catch (Exception e) {
                    log.warn("Failed to trigger scheduled discovery for connection {}: {}",
                        conn.getId(), e.getMessage());
                }
            }
        });
    }
}
