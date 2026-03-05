package com.derbysoft.click.modules.googleadsmanagement.domain;

import com.derbysoft.click.modules.googleadsmanagement.domain.aggregates.GoogleConnection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoogleConnectionRepository {
    Optional<GoogleConnection> findById(UUID id);
    Optional<GoogleConnection> findByTenantId(UUID tenantId);
    List<GoogleConnection> findAllActive();
    GoogleConnection save(GoogleConnection connection);
}
