package io.github.bsayli.licensing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(
    String serverUrl,
    String realm,
    String clientId,
    String clientSecret,
    Integer timeout,
    Pool pool) {

  public record Pool(Integer maxTotal, Integer maxPerRoute) {}
}
