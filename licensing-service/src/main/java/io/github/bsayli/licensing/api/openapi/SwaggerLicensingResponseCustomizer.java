package io.github.bsayli.licensing.api.openapi;

import static io.github.bsayli.licensing.common.openapi.OpenApiSchemas.SCHEMA_API_RESPONSE;

import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.common.openapi.ApiResponseSchemaFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerLicensingResponseCustomizer {

  private static final String REF_LICENSE_VALIDATION_RESPONSE =
      LicenseValidationResponse.class.getSimpleName();

  private static String apiResponseWrapperNameFor(String ref) {
    return SCHEMA_API_RESPONSE + ref;
  }

  @Bean
  public OpenApiCustomizer licensingWrappers() {
    return openApi ->
        openApi
            .getComponents()
            .addSchemas(
                apiResponseWrapperNameFor(REF_LICENSE_VALIDATION_RESPONSE),
                ApiResponseSchemaFactory.createComposedWrapper(REF_LICENSE_VALIDATION_RESPONSE));
  }
}
