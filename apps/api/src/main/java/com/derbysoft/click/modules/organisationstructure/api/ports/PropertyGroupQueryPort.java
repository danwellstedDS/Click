package com.derbysoft.click.modules.organisationstructure.api.ports;

import com.derbysoft.click.modules.organisationstructure.api.contracts.PropertyGroupInfo;
import java.util.Optional;
import java.util.UUID;

public interface PropertyGroupQueryPort {
  Optional<PropertyGroupInfo> findInfoById(UUID id);
  Optional<PropertyGroupInfo> findInfoByPrimaryOrgId(UUID primaryOrgId);
}
