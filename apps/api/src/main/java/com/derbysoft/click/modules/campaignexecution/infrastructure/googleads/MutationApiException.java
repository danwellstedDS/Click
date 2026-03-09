package com.derbysoft.click.modules.campaignexecution.infrastructure.googleads;

import com.derbysoft.click.modules.campaignexecution.domain.valueobjects.FailureClass;

public class MutationApiException extends RuntimeException {

    private final FailureClass failureClass;

    public MutationApiException(FailureClass failureClass, String message, Throwable cause) {
        super(message, cause);
        this.failureClass = failureClass;
    }

    public FailureClass getFailureClass() {
        return failureClass;
    }
}
