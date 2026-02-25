package api.presentation;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot configuration for @WebMvcTest slices.
 * Scans only the "api" package to avoid picking up persistence-layer
 * @Configuration classes (FlywayConfig) and the @EnableJpaRepositories
 * declaration on ApiApplication, both of which require JPA/DataSource
 * infrastructure that isn't present in a web-layer test slice.
 */
@SpringBootApplication(scanBasePackages = {"api"})
class TestWebConfig {}
