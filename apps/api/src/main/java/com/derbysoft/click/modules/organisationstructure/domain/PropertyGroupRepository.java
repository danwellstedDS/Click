package com.derbysoft.click.modules.organisationstructure.domain;

import com.derbysoft.click.modules.organisationstructure.domain.aggregates.PropertyGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyGroupRepository {
  Optional<PropertyGroup> findById(UUID id);
  Optional<PropertyGroup> findByPrimaryOrgId(UUID primaryOrgId);
  PropertyGroup create(String name);
  List<PropertyGroup> findAll();
  PropertyGroup save(PropertyGroup chain);
}
