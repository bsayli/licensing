package io.github.bsayli.licensing.client.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.client.adapter.config.LicensingServiceApiClientConfig;
import io.github.bsayli.licensing.client.adapter.impl.LicensingServiceClientAdapterImpl;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(
        classes = {
                LicensingServiceApiClientConfig.class,
                LicensingServiceClientAdapterImpl.class,
                LicensingServiceClientAdapterIT.TestBeans.class
        })
class LicensingServiceClientAdapterIT {

    static MockWebServer server;

    @Autowired
    private LicensingServiceClientAdapter adapter;

    @BeforeAll
    static void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
        System.setProperty(
                "licensing-service-api.base-url", server.url("/licensing-service").toString());
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.shutdown();
        System.clearProperty("licensing-service-api.base-url");
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 200 OK + LicenseAccessResponse")
    void issueAccess_shouldReturn200_andMappedBody() throws Exception {
        String body =
                """
                        {
                          "status": 200,
                          "message": "License is valid",
                          "data": {
                            "status": "TOKEN_CREATED",
                            "licenseToken": "jwt-abc"
                          },
                          "errors": []
                        }
                        """;

        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody(body));

        IssueAccessRequest req =
                new IssueAccessRequest()
                        .serviceId("crm")
                        .serviceVersion("1.5.0")
                        .instanceId("crm~host~mac")
                        .licenseKey("BSAYLI~RND~ENC")
                        .signature("BASE64SIG");

        ServiceResponse<LicenseAccessResponse> resp = adapter.issueAccess(req);

        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertNotNull(resp.getData().getStatus());
        assertEquals("TOKEN_CREATED", resp.getData().getStatus().getValue());
        assertEquals("jwt-abc", resp.getData().getLicenseToken());

        RecordedRequest rr = server.takeRequest();
        assertEquals("POST", rr.getMethod());
        assertEquals("/licensing-service/v1/licenses/access", rr.getPath());
    }

    @Test
    @DisplayName("POST /v1/licenses/access/validate -> 200 OK + LicenseAccessResponse (+header)")
    void validateAccess_shouldReturn200_andMappedBody_andSendHeader() throws Exception {
        String body =
                """
                        {
                          "status": 200,
                          "message": "License is valid",
                          "data": {
                            "status": "TOKEN_ACTIVE",
                            "licenseToken": null
                          },
                          "errors": []
                        }
                        """;

        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody(body));

        ValidateAccessRequest req =
                new ValidateAccessRequest()
                        .serviceId("crm")
                        .serviceVersion("1.5.0")
                        .instanceId("crm~host~mac")
                        .signature("BASE64SIG");

        String jwt = "jwt-123";
        ServiceResponse<LicenseAccessResponse> resp = adapter.validateAccess(jwt, req);

        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertNotNull(resp.getData().getStatus());
        assertEquals("TOKEN_ACTIVE", resp.getData().getStatus().getValue());

        RecordedRequest rr = server.takeRequest();
        assertEquals("POST", rr.getMethod());
        assertEquals("/licensing-service/v1/licenses/access/validate", rr.getPath());
        assertEquals(jwt, rr.getHeader("License-Token"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 404 -> ApiProblemException (ProblemDetail parsed)")
    void issueAccess_shouldThrowApiProblemException_whenProblemJsonReturned(){
        String body =
                """
                {
                  "type": "urn:licensing:problem:not-found",
                  "title": "Not found",
                  "status": 404,
                  "detail": "License not found.",
                  "instance": "/licensing-service/v1/licenses/access",
                  "errorCode": "NOT_FOUND",
                  "extensions": {
                    "errors": [
                      { "code": "NOT_FOUND", "message": "license missing", "resource": "License" }
                    ]
                  }
                }
                """;

        server.enqueue(
                new MockResponse()
                        .setResponseCode(404)
                        .addHeader("Content-Type", "application/problem+json")
                        .setBody(body));

        IssueAccessRequest req =
                new IssueAccessRequest()
                        .serviceId("crm")
                        .serviceVersion("1.5.0")
                        .instanceId("crm~host~mac")
                        .licenseKey("BSAYLI~RND~ENC")
                        .signature("BASE64SIG");

        ApiProblemException ex =
                assertThrows(ApiProblemException.class, () -> adapter.issueAccess(req));

        assertEquals(404, ex.getStatus());
        assertEquals("NOT_FOUND", ex.getErrorCode());
        assertTrue(ex.hasErrors());
        assertNotNull(ex.firstErrorOrNull());
        assertEquals("NOT_FOUND", ex.firstErrorOrNull().getCode());
        assertEquals("License", ex.firstErrorOrNull().getResource());
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 502 + text/plain -> ApiProblemException (fallback: non-json)")
    void issueAccess_shouldThrowApiProblemException_whenNonJsonErrorReturned() {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(502)
                        .addHeader("Content-Type", "text/plain")
                        .setBody("bad gateway"));

        IssueAccessRequest req =
                new IssueAccessRequest()
                        .serviceId("crm")
                        .serviceVersion("1.5.0")
                        .instanceId("crm~host~mac")
                        .licenseKey("BSAYLI~RND~ENC")
                        .signature("BASE64SIG");

        ApiProblemException ex =
                assertThrows(ApiProblemException.class, () -> adapter.issueAccess(req));

        assertEquals(502, ex.getStatus());
        assertEquals("UPSTREAM_NON_JSON_ERROR", ex.getErrorCode());
        assertNotNull(ex.getProblem());
        assertNotNull(ex.getProblem().getType());
        assertTrue(ex.getProblem().getType().toString().contains("upstream-non-json"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 500 + empty body -> ApiProblemException (fallback: empty-body)")
    void issueAccess_shouldThrowApiProblemException_whenEmptyBodyReturned() throws Exception {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .addHeader("Content-Type", "application/problem+json")
                        .setBody(""));

        IssueAccessRequest req =
                new IssueAccessRequest()
                        .serviceId("crm")
                        .serviceVersion("1.5.0")
                        .instanceId("crm~host~mac")
                        .licenseKey("BSAYLI~RND~ENC")
                        .signature("BASE64SIG");

        ApiProblemException ex =
                assertThrows(ApiProblemException.class, () -> adapter.issueAccess(req));

        assertEquals(500, ex.getStatus());
        assertEquals("UPSTREAM_EMPTY_PROBLEM", ex.getErrorCode());
        assertNotNull(ex.getProblem());
        assertNotNull(ex.getProblem().getType());
        assertTrue(ex.getProblem().getType().toString().contains("upstream-empty"));
    }

    @Configuration
    static class TestBeans {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

    }
}
