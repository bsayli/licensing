package io.github.bsayli.licensing.agent.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.api.exception.ApiRequestExceptionHandler;
import io.github.bsayli.licensing.agent.api.exception.ApplicationExceptionHandler;
import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.agent.testconfig.TestControllerMocksConfig;
import io.github.bsayli.licensing.agent.testconfig.TestWebMvcSecurityConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LicenseAgentController.class)
@Import({
        ApiRequestExceptionHandler.class,
        ApplicationExceptionHandler.class,
        TestWebMvcSecurityConfig.class,
        TestControllerMocksConfig.class
})
@Tag("integration")
class LicenseAgentControllerIT {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private LicenseOrchestrationService service;

    @Test
    @DisplayName("POST /v1/licenses/access -> 200 returns ServiceResponse<LicenseToken>")
    void obtainToken_ok() throws Exception {
        var req =
                new LicenseAccessRequest(
                        "L".repeat(100) + "~rnd~" + "A".repeat(64),
                        "licensing-service~demo~00:11:22:33:44:55",
                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8a5",
                        "crm",
                        "1.5.0");

        Mockito.when(service.getLicenseToken(any(LicenseAccessRequest.class)))
                .thenReturn(new LicenseToken("jwt-token"));

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.licenseToken").value("jwt-token"))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.serverTime").exists());
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 400 validation error")
    void obtainToken_validationError() throws Exception {
        var bad = new LicenseAccessRequest("x", "short", null, "c", "1");

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(4))
                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 400 invalid JSON")
    void obtainToken_badJson_notReadable() throws Exception {
        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> unknown field -> 400")
    void obtainToken_badJson_unrecognizedField() throws Exception {
        String body =
                """
                {
                  "licenseKey": "%s",
                  "instanceId": "licensing-service~demo~00:11:22:33:44:55",
                  "checksum": "%s",
                  "serviceId": "crm",
                  "serviceVersion": "1.5.0",
                  "foo": "bar"
                }
                """.formatted("L".repeat(120), "c".repeat(40));

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Unrecognized field: 'foo'"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("GET /v1/licenses/access -> 405")
    void obtainToken_methodNotAllowed() throws Exception {
        mvc.perform(get("/v1/licenses/access"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> remote service exception")
    void obtainToken_remoteServiceError() throws Exception {
        var req =
                new LicenseAccessRequest(
                        "L".repeat(100) + "~rnd~" + "A".repeat(64),
                        "licensing-service~demo~00:11:22:33:44:55",
                        "c".repeat(40),
                        "crm",
                        "1.5.0");

        Mockito.when(service.getLicenseToken(any(LicenseAccessRequest.class)))
                .thenThrow(
                        new LicensingAgentRemoteServiceException(
                                HttpStatus.BAD_GATEWAY,
                                "REMOTE_ERROR",
                                "Remote call failed",
                                List.of("x : y")));

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("REMOTE_ERROR"))
                .andExpect(jsonPath("$.message").value("Remote call failed"));
    }
}