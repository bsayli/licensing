package io.github.bsayli.licensing.agent.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.version:${project.version:unknown}}")
    private String version;

    @Value("${app.openapi.base-url:}")
    private String baseUrl;

    @Bean
    public OpenAPI licensingServiceOpenAPI() {
        var openapi =
                new OpenAPI()
                        .components(new Components())
                        .info(
                                new Info()
                                        .title(OpenApiConstants.TITLE)
                                        .version(version)
                                        .description(OpenApiConstants.DESCRIPTION));

        if (baseUrl != null && !baseUrl.isBlank()) {
            openapi.addServersItem(
                    new Server().url(baseUrl).description(OpenApiConstants.SERVER_DESCRIPTION));
        }

        openapi
                .getComponents()
                .addSecuritySchemes(
                        "basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"));

        openapi.addSecurityItem(new SecurityRequirement().addList("basicAuth"));

        return openapi;
    }
}