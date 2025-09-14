package io.github.bsayli.licensing.sdk.testconfig;

import io.github.bsayli.licensing.sdk.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.sdk.service.LicenseOrchestrationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestControllerMocksConfig {

  @Bean
  @Primary
  public LicenseOrchestrationService licenseOrchestrationService() {
    return Mockito.mock(LicenseOrchestrationService.class);
  }

  @Bean
  @Primary
  public LocalizedMessageResolver messageResolver() {
    var mr = Mockito.mock(LocalizedMessageResolver.class);
    Mockito.when(mr.getMessage("license.validation.success")).thenReturn("License is valid");
    Mockito.when(mr.getMessage("request.validation.failed"))
        .thenReturn("Request validation failed");
    Mockito.when(mr.getMessage("license.validation.error"))
        .thenReturn("An unexpected error occurred");
    return mr;
  }
}
