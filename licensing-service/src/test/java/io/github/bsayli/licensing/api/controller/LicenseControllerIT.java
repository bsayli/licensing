package io.github.bsayli.licensing.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.api.exception.LicenseControllerAdvice;
import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.testconfig.TestControllerMocksConfig;
import io.github.bsayli.licensing.testconfig.TestWebMvcSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LicenseController.class)
@Import({
  LicenseControllerAdvice.class,
  TestWebMvcSecurityConfig.class,
  TestControllerMocksConfig.class
})
@Tag("integration")
class LicenseControllerIT {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper om;
  @Autowired private LicenseOrchestrationService service;
  @Autowired private LocalizedMessageResolver messageResolver;

  private static String fakeJwt() {
    String p1 = "A".repeat(80);
    String p2 = "b".repeat(80);
    String p3 = "C".repeat(80);
    return p1 + "." + p2 + "." + p3;
  }

  @Test
  @DisplayName("POST /v1/licenses/access -> 200 returns token")
  void createAccess_ok() throws Exception {
    var req =
        new IssueAccessRequest(
            "L".repeat(100) + "~rnd~" + "A".repeat(64),
            "licensing-service~demo~00:11:22:33:44:55",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8a5",
            "crm",
            "1.5.0",
            "Q".repeat(60));

    Mockito.when(service.issueAccess(req)).thenReturn(LicenseAccessResponse.created("jwt-token"));
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
  void createAccess_validationError() throws Exception {
    var bad = new IssueAccessRequest("x", "short", null, "c", "1", "y");

    mvc.perform(
            post("/v1/licenses/access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Request validation failed"))
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  @DisplayName("POST /v1/licenses/access/validate -> 200 returns refreshed token")
  void validateAccess_ok_refreshed() throws Exception {
    var req =
        new ValidateAccessRequest(
            "licensing-service~demo~00:11:22:33:44:55",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8a5",
            "crm",
            "1.5.0",
            "Q".repeat(60));

    String jwt = fakeJwt();

    Mockito.when(service.validateAccess(req, jwt))
        .thenReturn(LicenseAccessResponse.refreshed("new-jwt"));
    Mockito.when(messageResolver.getMessage("license.validation.success"))
        .thenReturn("License is valid");

    mvc.perform(
            post("/v1/licenses/access/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("License-Token", jwt)
                .content(om.writeValueAsBytes(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.licenseToken").value("new-jwt"));
  }

  @Test
  @DisplayName("POST /v1/licenses/access/validate -> 400 invalid or missing header JWT")
  void validateAccess_badHeader() throws Exception {
    var req =
        new ValidateAccessRequest(
            "licensing-service~demo~00:11:22:33:44:55", null, "crm", "1.5.0", "Q".repeat(60));

    // Missing header
    mvc.perform(
            post("/v1/licenses/access/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(req)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.errors").isArray());

    // Not 3-part JWT
    mvc.perform(
            post("/v1/licenses/access/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("License-Token", "abc.def")
                .content(om.writeValueAsBytes(req)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.errors").isArray());
  }
}
