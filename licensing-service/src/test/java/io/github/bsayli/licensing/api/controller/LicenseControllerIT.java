package io.github.bsayli.licensing.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.api.exception.ApplicationExceptionHandler;
import io.github.bsayli.licensing.api.exception.JsonExceptionHandler;
import io.github.bsayli.licensing.api.exception.SpringHttpExceptionHandler;
import io.github.bsayli.licensing.api.exception.ValidationExceptionHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LicenseController.class)
@Import({
        ValidationExceptionHandler.class,
        JsonExceptionHandler.class,
        SpringHttpExceptionHandler.class,
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
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Required header 'License-Token' is not present."))
                .andExpect(jsonPath("$.instance").value("/v1/licenses/access/validate"))
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.extensions").doesNotExist());
    }
}