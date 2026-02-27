package com.derbysoft.click.bootstrap.persistence;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA and Flyway configuration.
 *
 * <p>Scopes entity scanning and JPA repository detection to the modules layer,
 * making the persistence infrastructure explicit and bounded.
 * Flyway is auto-configured from application.yml (spring.flyway.*).
 */
@Configuration
@EntityScan(basePackages = "com.derbysoft.click.modules")
@EnableJpaRepositories(basePackages = "com.derbysoft.click.modules")
public class DbConfig {
}
