package com.derbysoft.click;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot configuration for {@code @WebMvcTest} slices.
 * Limits component scanning to exclude persistence-layer configuration
 * ({@code DbConfig}, {@code ModuleRegistry}) that requires a DataSource,
 * which is not available in web-layer test slices.
 */
@SpringBootApplication(scanBasePackages = {
    "com.derbysoft.click.bootstrap.web",
    "com.derbysoft.click.bootstrap.messaging",
    "com.derbysoft.click.modules",
    "com.derbysoft.click.sharedkernel"
})
class TestWebConfig {}
