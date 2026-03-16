package io.github.bsayli.licensing.agent.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.api.exception.ApiRequestExceptionHandler;
import io.github.bsayli.licensing.agent.api.exception.ApplicationExceptionHandler;
import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.agent.testconfig.TestControllerMocksConfig;
import io.github.bsayli.licensing.agent.testconfig.TestWebMvcSecurityConfig;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LicenseController.class)
@Import({
        ApiRequestExceptionHandler.class,
        ApplicationExceptionHandler.class,
        TestWebMvcSecurityConfig.class,
        TestControllerMocksConfig.class
})
@Tag("integration")
class LicenseControllerIT {

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
                .andExpect(jsonPath("$.meta.serverTime").exists())
                .andExpect(jsonPath("$.meta.sort").isArray());
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 400 validation error (MethodArgumentNotValid)")
    void obtainToken_validationError_methodArgumentNotValid() throws Exception {
        var bad = new LicenseAccessRequest("x", "short", null, "c", "1");

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-agent:problem:validation-failed"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.extensions").exists())
                .andExpect(jsonPath("$.extensions.errors").isArray())
                .andExpect(jsonPath("$.extensions.errors.length()").value(4))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 400 invalid JSON (HttpMessageNotReadable)")
    void obtainToken_badJson_notReadable() throws Exception {
        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-agent:problem:bad-request"))
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Malformed request body."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].message").value("Invalid JSON payload."));
    }


    @Test
    @DisplayName("POST /v1/licenses/access -> unknown field -> 400 bad request")
    void obtainToken_badJson_unrecognizedField_returnsProblemDetail_badRequest() throws Exception {
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
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-agent:problem:bad-request"))
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Malformed request body."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].message").value("Unrecognized field: 'foo'"))
                .andExpect(jsonPath("$.extensions.errors[0].field").value("foo"));
    }

    @Test
    @DisplayName("GET /v1/licenses/access -> 405 method not allowed")
    void obtainToken_methodNotAllowed() throws Exception {
        mvc.perform(get("/v1/licenses/access"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-agent:problem:method-not-allowed"))
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.detail").value("The request method is not supported for this resource."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.extensions.errors[0].message").value("HTTP method not supported: GET"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> remote service exception mapped (ApplicationExceptionHandler)")
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
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-agent:problem:service-error"))
                .andExpect(jsonPath("$.title").value("License validation failed"))
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.detail").value("Remote call failed"))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("REMOTE_ERROR"))
                .andExpect(jsonPath("$.extensions").exists())
                .andExpect(jsonPath("$.extensions.errors").isArray())
                .andExpect(jsonPath("$.extensions.errors[0].code").value("REMOTE_ERROR"))
                .andExpect(jsonPath("$.extensions.errors[0].message").value("x : y"));
    }
}