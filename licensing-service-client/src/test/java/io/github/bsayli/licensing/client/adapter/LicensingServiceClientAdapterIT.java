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
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

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
                          "data": {
                            "status": "TOKEN_CREATED",
                            "licenseToken": "jwt-abc"
                          },
                          "meta": {
                            "serverTime": "2026-03-16T10:03:55.763658Z",
                            "sort": []
                          }
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
        assertNotNull(resp.getMeta());

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
                          "data": {
                            "status": "TOKEN_ACTIVE",
                            "licenseToken": null
                          },
                          "meta": {
                            "serverTime": "2026-03-16T10:03:55.763658Z",
                            "sort": []
                          }
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
        assertNotNull(resp.getMeta());

        RecordedRequest rr = server.takeRequest();
        assertEquals("POST", rr.getMethod());
        assertEquals("/licensing-service/v1/licenses/access/validate", rr.getPath());
        assertEquals(jwt, rr.getHeader("License-Token"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 404 -> ApiProblemException (Spring ProblemDetail parsed)")
    void issueAccess_shouldThrowApiProblemException_whenProblemJsonReturned() {
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

        ProblemDetail pd = ex.getProblem();
        assertNotNull(pd);
        assertEquals("Not found", pd.getTitle());
        assertEquals("License not found.", pd.getDetail());
        assertNotNull(pd.getProperties());

        Object errorCode = pd.getProperties().get("errorCode");
        assertEquals("NOT_FOUND", errorCode);

        Object extensions = pd.getProperties().get("extensions");
        assertInstanceOf(Map.class, extensions);

        @SuppressWarnings("unchecked")
        Map<String, Object> extensionsMap = (Map<String, Object>) extensions;

        Object errors = extensionsMap.get("errors");
        assertInstanceOf(List.class, errors);

        @SuppressWarnings("unchecked")
        List<Object> errorList = (List<Object>) errors;

        assertEquals(1, errorList.size());
        assertInstanceOf(Map.class, errorList.getFirst());

        @SuppressWarnings("unchecked")
        Map<String, Object> firstError = (Map<String, Object>) errorList.getFirst();

        assertEquals("NOT_FOUND", firstError.get("code"));
        assertEquals("license missing", firstError.get("message"));
        assertEquals("License", firstError.get("resource"));
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

        ProblemDetail pd = ex.getProblem();
        assertNotNull(pd);
        assertNotNull(pd.getType());
        assertTrue(pd.getType().toString().contains("upstream-non-json"));
        assertNotNull(pd.getProperties());
        assertEquals("UPSTREAM_NON_JSON_ERROR", pd.getProperties().get("errorCode"));
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> 500 + empty body -> ApiProblemException (fallback: empty-body)")
    void issueAccess_shouldThrowApiProblemException_whenEmptyBodyReturned() {
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

        ProblemDetail pd = ex.getProblem();
        assertNotNull(pd);
        assertNotNull(pd.getType());
        assertTrue(pd.getType().toString().contains("upstream-empty"));
        assertNotNull(pd.getProperties());
        assertEquals("UPSTREAM_EMPTY_PROBLEM", pd.getProperties().get("errorCode"));
    }

    @Configuration
    static class TestBeans {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        ObjectMapper objectMapper() {
            return Jackson2ObjectMapperBuilder.json().build();
        }
    }
}