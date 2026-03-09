package com.derbysoft.click.modules.campaignexecution.domain.valueobjects;

public enum ApplyOrder {
    CAMPAIGN(1), AD_GROUP(2), AD(3), KEYWORD(4);

    private final int order;

    ApplyOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
