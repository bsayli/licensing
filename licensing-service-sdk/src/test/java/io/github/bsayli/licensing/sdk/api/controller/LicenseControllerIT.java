package io.github.bsayli.licensing.sdk.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseToken;
import io.github.bsayli.licensing.sdk.api.exception.LicenseControllerAdvice;
import io.github.bsayli.licensing.sdk.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.sdk.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.sdk.testconfig.TestControllerMocksConfig;
import io.github.bsayli.licensing.sdk.testconfig.TestRestClientConfig;
import io.github.bsayli.licensing.sdk.testconfig.TestWebMvcSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = LicenseController.class,
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = io.github.bsayli.licensing.sdk.config.security.SecurityConfig.class),
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = "io\\.github\\.bsayli\\.licensing\\.client\\..*")
    })
@Import({
  LicenseControllerAdvice.class,
  TestWebMvcSecurityConfig.class,
  TestControllerMocksConfig.class,
  TestRestClientConfig.class
})
@Tag("integration")
class LicenseControllerIT {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper om;
  @Autowired private LicenseOrchestrationService service;
  @Autowired private LocalizedMessageResolver messageResolver;

  @Test
  @DisplayName("POST /v1/licenses/access -> 200 returns token")
  void obtainToken_ok() throws Exception {
    var req =
        new LicenseAccessRequest(
            "L".repeat(100) + "~rnd~" + "A".repeat(64),
            "licensing-service~demo~00:11:22:33:44:55",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8a5",
            "crm",
            "1.5.0");

    Mockito.when(service.getLicenseToken(req)).thenReturn(new LicenseToken("jwt-token"));
    Mockito.when(messageResolver.getMessage("license.validation.success"))
        .thenReturn("License is valid");

    mvc.perform(
            post("/v1/licenses/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("License is valid"))
        .andExpect(jsonPath("$.data.licenseToken").value("jwt-token"));
  }

  @Test
  @DisplayName("POST /v1/licenses/access -> 400 validation error")
  void obtainToken_validationError() throws Exception {
    var bad = new LicenseAccessRequest("x", "short", null, "c", "1");

    Mockito.when(messageResolver.getMessage("request.validation.failed"))
        .thenReturn("Request validation failed");

    mvc.perform(
            post("/v1/licenses/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Request validation failed"))
        .andExpect(jsonPath("$.errors").isArray());
  }
}
