package io.github.bsayli.licensing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retry")
public record RetryProperties(Spec userService, Spec userServiceAsync) {
  public record Spec(int maxAttempts, long initialDelay, long multiplier, long maxDelay) {}
}
