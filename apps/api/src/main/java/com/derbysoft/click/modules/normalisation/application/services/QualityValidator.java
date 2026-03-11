package com.derbysoft.click.modules.normalisation.application.services;

import com.derbysoft.click.modules.normalisation.application.ports.RawCampaignRowQueryPort.RawCampaignRowData;
import com.derbysoft.click.modules.normalisation.domain.valueobjects.QualityFlag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QualityValidator {

    public List<QualityFlag> validate(RawCampaignRowData row) {
        List<QualityFlag> flags = new ArrayList<>();
        if (row.impressions() < 0) flags.add(QualityFlag.NEGATIVE_IMPRESSIONS);
        if (row.clicks() < 0) flags.add(QualityFlag.NEGATIVE_CLICKS);
        if (row.costMicros() < 0) flags.add(QualityFlag.NEGATIVE_COST);
        if (row.conversions() != null && row.conversions().signum() < 0) flags.add(QualityFlag.NEGATIVE_CONVERSIONS);
        if (row.campaignId() == null || row.campaignId().isBlank()) flags.add(QualityFlag.MISSING_CAMPAIGN_ID);
        return flags;
    }
}
