package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.modules.googleadsmanagement.api.ports.GoogleAdsQueryPort;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyIngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyIngestionScheduler.class);

    private final IngestionJobService ingestionJobService;
    private final GoogleAdsQueryPort googleAdsQueryPort;

    public DailyIngestionScheduler(IngestionJobService ingestionJobService,
                                    GoogleAdsQueryPort googleAdsQueryPort) {
        this.ingestionJobService = ingestionJobService;
        this.googleAdsQueryPort = googleAdsQueryPort;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void scheduleDailyJobs() {
        // MVP: fire when UTC hour == 2 and minute < 5 (global default 02:00 UTC)
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        if (now.getHour() != 2 || now.getMinute() >= 5) {
            return;
        }

        var connections = googleAdsQueryPort.findAllActiveConnections();
        for (var connection : connections) {
            try {
                ingestionJobService.enqueueDailySync(connection.id(), connection.tenantId());
            } catch (Exception e) {
                log.warn("Failed to enqueue daily sync for tenant {}: {}",
                    connection.tenantId(), e.getMessage());
            }
        }
    }
}
