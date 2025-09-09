package io.github.bsayli.licensing.common.openapi;

import static io.github.bsayli.licensing.common.openapi.OpenApiConstants.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${app.openapi.base-url:}")
  private String baseUrl;

  @Bean
  public OpenAPI licensingServiceOpenAPI() {
    var openapi =
        new OpenAPI().info(new Info().title(TITLE).version(VERSION).description(DESCRIPTION));

    if (baseUrl != null && !baseUrl.isBlank()) {
      openapi.addServersItem(new Server().url(baseUrl).description(SERVER_DESCRIPTION));
    }
    return openapi;
  }
}
