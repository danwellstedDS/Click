package com.derbysoft.click.bootstrap.di;

import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository.PropertyGroupJpaRepository;
import com.derbysoft.click.modules.organisationstructure.infrastructure.persistence.repository.PropertyGroupRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires cross-BC dependencies. Each {@code @Bean} here represents a deliberate
 * dependency from one bounded context to another's public API port.
 *
 * <p>BC1 (identity-access) → BC3 (organisation-structure): PropertyGroupQueryPort
 *
 * <p>{@code PropertyGroupRepositoryImpl} implements both {@code PropertyGroupRepository}
 * (BC3 domain port) and {@code PropertyGroupQueryPort} (BC3 public API port). Declaring
 * it here rather than via {@code @Repository} component scan makes the cross-BC wiring
 * explicit. Spring auto-wires it wherever either interface is required.
 */
@Configuration
public class ModuleRegistry {

  @Bean
  public PropertyGroupRepositoryImpl propertyGroupRepositoryImpl(
      PropertyGroupJpaRepository jpaRepository) {
    return new PropertyGroupRepositoryImpl(jpaRepository);
  }
}
