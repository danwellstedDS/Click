package com.derbysoft.click.modules.ingestion.application.ports;

import com.derbysoft.click.modules.ingestion.domain.valueobjects.DateWindow;
import java.time.LocalDate;
import java.util.List;

public interface GoogleAdsReportingPort {

    List<CampaignRow> fetchCampaignMetrics(String customerId, String managerId,
                                            String credentialPath, DateWindow window);

    record CampaignRow(
        String campaignId,
        String campaignName,
        long clicks,
        long impressions,
        long costMicros,
        double conversions,
        LocalDate reportDate
    ) {}
}
