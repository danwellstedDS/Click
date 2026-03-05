package com.derbysoft.click.modules.googleadsmanagement.api.ports;

import com.derbysoft.click.modules.googleadsmanagement.api.contracts.AccountBindingInfo;
import com.derbysoft.click.modules.googleadsmanagement.api.contracts.GoogleAdsConnectionInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoogleAdsQueryPort {
    Optional<GoogleAdsConnectionInfo> findConnectionByTenantId(UUID tenantId);
    List<AccountBindingInfo> listActiveBindings(UUID tenantId);
}
