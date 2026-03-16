package io.github.bsayli.licensing.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.api.exception.ApiRequestExceptionHandler;
import io.github.bsayli.licensing.api.exception.ApplicationExceptionHandler;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.testconfig.TestControllerMocksConfig;
import io.github.bsayli.licensing.testconfig.TestWebMvcSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LicenseController.class)
@Import({
        ApiRequestExceptionHandler.class,
        ApplicationExceptionHandler.class,
        TestControllerMocksConfig.class,
        TestWebMvcSecurityConfig.class,
})
class LicenseControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Autowired
    LicenseOrchestrationService service;

    private static String repeat(char c, int n) {
        return String.valueOf(c).repeat(Math.max(0, n));
    }

    private static String jwtLikeToken(int a, int b, int c) {
        return repeat('a', a) + "." + repeat('b', b) + "." + repeat('c', c);
    }

    @Test
    void createAccess_ok_returnsServiceResponseWithMeta() throws Exception {
        IssueAccessRequest req =
                new IssueAccessRequest(
                        repeat('k', 120),
                        repeat('i', 10),
                        repeat('c', 40),
                        "crm",
                        "1.0",
                        repeat('s', 60));

        Mockito.when(service.issueAccess(any(IssueAccessRequest.class)))
                .thenReturn(LicenseAccessResponse.created("jwt-token"));

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").value("TOKEN_CREATED"))
                .andExpect(jsonPath("$.data.licenseToken").value("jwt-token"))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.serverTime").exists())
                .andExpect(jsonPath("$.meta.sort").isArray());
    }

    @Test
    void validateAccess_ok_returnsServiceResponseWithMeta() throws Exception {
        String token = jwtLikeToken(80, 80, 80);

        ValidateAccessRequest req =
                new ValidateAccessRequest(
                        repeat('i', 10),
                        repeat('c', 40),
                        "crm",
                        "1.0",
                        repeat('s', 60));

        Mockito.when(service.validateAccess(any(ValidateAccessRequest.class), eq(token)))
                .thenReturn(LicenseAccessResponse.active("jwt-token"));

        mvc.perform(
                        post("/v1/licenses/access/validate")
                                .header("License-Token", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").value("TOKEN_ACTIVE"))
                .andExpect(jsonPath("$.data.licenseToken").value("jwt-token"))
                .andExpect(jsonPath("$.meta").exists())
                .andExpect(jsonPath("$.meta.serverTime").exists())
                .andExpect(jsonPath("$.meta.sort").isArray());
    }

    @Test
    void createAccess_validationError_returnsProblemDetail() throws Exception {
        IssueAccessRequest req =
                new IssueAccessRequest(
                        "short",
                        "tiny",
                        "x",
                        "c",
                        "1",
                        "sig");

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-service:problem:validation-failed"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.extensions.errors").isArray())
                .andExpect(jsonPath("$.extensions.errors.length()").value(6));
    }

    @Test
    void validateAccess_bodyValidationError_returnsProblemDetail() throws Exception {
        String token = jwtLikeToken(80, 80, 80);

        ValidateAccessRequest req =
                new ValidateAccessRequest(
                        "tiny",
                        "x",
                        "c",
                        "1",
                        "sig");

        mvc.perform(
                        post("/v1/licenses/access/validate")
                                .header("License-Token", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-service:problem:validation-failed"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access/validate"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.extensions.errors").isArray())
                .andExpect(jsonPath("$.extensions.errors.length()").value(5));
    }

    @Test
    void validateAccess_missingHeader_returnsProblemDetail_badRequest() throws Exception {
        ValidateAccessRequest req =
                new ValidateAccessRequest(
                        repeat('i', 10),
                        repeat('c', 40),
                        "crm",
                        "1.0",
                        repeat('s', 60));

        mvc.perform(
                        post("/v1/licenses/access/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-service:problem:bad-request"))
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more parameters are invalid."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access/validate"))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].field").value("License-Token"));
    }

    @Test
    void validateAccess_invalidHeaderFormat_returnsProblemDetail_validationFailed() throws Exception {
        String invalidToken = "invalid-token";

        ValidateAccessRequest req =
                new ValidateAccessRequest(
                        repeat('i', 10),
                        repeat('c', 40),
                        "crm",
                        "1.0",
                        repeat('s', 60));

        mvc.perform(
                        post("/v1/licenses/access/validate")
                                .header("License-Token", invalidToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsBytes(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-service:problem:validation-failed"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access/validate"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.extensions.errors").isArray());
    }

    @Test
    void createAccess_invalidJson_returnsProblemDetail_badRequest() throws Exception {
        String body = """
                {
                  "licenseKey":
                """;

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-service:problem:bad-request"))
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Malformed request body."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"));
    }

    @Test
    void createAccess_unknownField_returnsProblemDetail_badRequest() throws Exception {
        String body = """
                {
                  "licenseKey": "%s",
                  "instanceId": "%s",
                  "checksum": "%s",
                  "serviceId": "crm",
                  "serviceVersion": "1.0",
                  "signature": "%s",
                  "foo": "bar"
                }
                """.formatted(
                repeat('k', 120),
                repeat('i', 10),
                repeat('c', 40),
                repeat('s', 60));

        mvc.perform(
                        post("/v1/licenses/access")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-service:problem:bad-request"))
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Malformed request body."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.extensions.errors[0].field").value("foo"));
    }

    @Test
    void createAccess_methodNotAllowed_returnsProblemDetail() throws Exception {
        mvc.perform(get("/v1/licenses/access"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:licensing-service:problem:method-not-allowed"))
                .andExpect(jsonPath("$.title").value("Method not allowed"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.detail").value("The request method is not supported for this resource."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access"))
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.extensions.errors[0].code").value("METHOD_NOT_ALLOWED"));
    }

}