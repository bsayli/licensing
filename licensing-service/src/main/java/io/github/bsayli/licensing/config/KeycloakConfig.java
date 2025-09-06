package io.github.bsayli.licensing.config;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

  private Keycloak keycloak;

  @Bean
  public Keycloak keycloak(KeycloakProperties properties) {
    ResteasyClientBuilder builder =
        (ResteasyClientBuilder) jakarta.ws.rs.client.ClientBuilder.newBuilder();

    ResteasyClient resteasyClient =
        builder
            .connectTimeout(properties.timeout(), TimeUnit.SECONDS)
            .readTimeout(properties.timeout(), TimeUnit.SECONDS)
            .build();

    this.keycloak =
        KeycloakBuilder.builder()
            .serverUrl(properties.serverUrl())
            .realm(properties.realm())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(properties.clientId())
            .clientSecret(properties.clientSecret())
            .resteasyClient(resteasyClient)
            .build();

    return this.keycloak;
  }

  @PreDestroy
  public void shutdown() {
    if (keycloak != null) {
      keycloak.close();
    }
  }
}
