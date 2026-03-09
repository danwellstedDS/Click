package com.derbysoft.click.modules.campaignexecution.application.handlers;

import com.derbysoft.click.modules.campaignexecution.domain.aggregates.WriteAction;
import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;
import com.derbysoft.click.modules.campaignexecution.infrastructure.googleads.MutationApiException;
import com.derbysoft.click.modules.campaignexecution.infrastructure.googleads.MutationAuthException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component("ceRetryPolicyEngine")
public class RetryPolicyEngine {

    private static final long MAX_DELAY_SECONDS = 900L;

    public FailureClass classify(Exception e) {
        if (e instanceof MutationAuthException) return FailureClass.PERMANENT;
        if (e instanceof MutationApiException mae) return mae.getFailureClass();
        return FailureClass.TRANSIENT;
    }

    public Duration computeDelay(WriteAction action) {
        long exponential = (long) Math.pow(2, action.getAttempts()) * 30L;
        long capped = Math.min(exponential, MAX_DELAY_SECONDS);
        long jitter = ThreadLocalRandom.current().nextLong(0, 31);
        return Duration.ofSeconds(capped + jitter);
    }
}
