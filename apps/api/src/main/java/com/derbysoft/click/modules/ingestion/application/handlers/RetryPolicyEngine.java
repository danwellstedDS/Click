package com.derbysoft.click.modules.ingestion.application.handlers;

import com.derbysoft.click.modules.ingestion.domain.aggregates.SyncJob;
import com.derbysoft.click.modules.ingestion.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionAuthException;
import com.derbysoft.click.modules.ingestion.infrastructure.googleads.IngestionFetchException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component("ingestionRetryPolicyEngine")
public class RetryPolicyEngine {

    private static final long MAX_DELAY_SECONDS = 1800L;

    public FailureClass classify(Exception e) {
        if (e instanceof IngestionAuthException) {
            return FailureClass.PERMANENT;
        }
        if (e instanceof IngestionFetchException ife) {
            return ife.getFailureClass();
        }
        return FailureClass.TRANSIENT;
    }

    public Duration computeDelay(SyncJob job) {
        long exponential = (long) Math.pow(2, job.getAttempts()) * 60L;
        long capped = Math.min(exponential, MAX_DELAY_SECONDS);
        long jitter = ThreadLocalRandom.current().nextLong(0, 31);
        return Duration.ofSeconds(capped + jitter);
    }
}
