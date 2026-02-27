package com.derbysoft.click.bootstrap.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private static final HealthStatus OK = new HealthStatus("ok");

  @GetMapping("/health")
  public HealthStatus health() {
    return OK;
  }

  @GetMapping("/api/health")
  public HealthStatus apiHealth() {
    return OK;
  }

  public record HealthStatus(String status) {}
}
