package io.github.bsayli.licensing.testconfig;

import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestControllerMocksConfig {

  @Bean
  public LicenseOrchestrationService licenseOrchestrationService() {
    return Mockito.mock(LicenseOrchestrationService.class);
  }

  @Bean
  public LocalizedMessageResolver messageResolver() {
    var mr = Mockito.mock(LocalizedMessageResolver.class);
    Mockito.when(mr.getMessage("license.validation.success")).thenReturn("License is valid");
    Mockito.when(mr.getMessage("request.validation.failed"))
        .thenReturn("Request validation failed");
    Mockito.when(mr.getMessage("license.validation.error")).thenReturn("Unexpected error");
    Mockito.when(mr.getMessage("request.header.missing", "License-Token"))
        .thenReturn("Header 'License-Token' is missing");
    return mr;
  }
}
