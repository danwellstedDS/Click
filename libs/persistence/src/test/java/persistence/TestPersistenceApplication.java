package persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot bootstrap for @DataJpaTest slices in this module.
 * @DataJpaTest requires a @SpringBootConfiguration somewhere in the package
 * hierarchy; library modules don't have one so we provide it here in test
 * sources only.
 */
@SpringBootApplication
class TestPersistenceApplication {}
