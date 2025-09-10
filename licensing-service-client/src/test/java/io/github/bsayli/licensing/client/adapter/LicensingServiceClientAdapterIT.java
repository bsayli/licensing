package io.github.bsayli.licensing.client.adapter;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.client.adapter.config.LicensingServiceApiClientConfig;
import io.github.bsayli.licensing.client.adapter.impl.LicensingServiceClientAdapterImpl;
import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.common.core.ApiClientExecutor;
import io.github.bsayli.licensing.client.common.core.ResponseParser;
import io.github.bsayli.licensing.client.generated.dto.IssueTokenRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateTokenRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

@SpringJUnitConfig(classes = {
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
        System.setProperty("licensing-service-api.base-url",
                server.url("/licensing-service").toString());
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.shutdown();
        System.clearProperty("licensing-service-api.base-url");
    }

    @Test
    @DisplayName("POST /v1/licenses/tokens -> 200 OK + LicenseValidationResponse")
    void issueToken_shouldReturn200_andMappedBody() throws Exception {
        String body = """
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

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        IssueTokenRequest req = new IssueTokenRequest()
                .serviceId("crm")
                .serviceVersion("1.5.0")
                .instanceId("crm~host~mac")
                .licenseKey("BSAYLI~RND~ENC")
                .signature("BASE64SIG");

        ApiClientResponse<LicenseValidationResponse> resp = adapter.issueToken(req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        assertEquals("License is valid", resp.getMessage());
        assertNotNull(resp.getData());
        assertNotNull(resp.getData().getStatus());
        assertEquals("TOKEN_CREATED", resp.getData().getStatus().getValue());
        assertEquals("jwt-abc", resp.getData().getLicenseToken());

        RecordedRequest rr = server.takeRequest();
        assertEquals("POST", rr.getMethod());
        assertEquals("/licensing-service/v1/licenses/tokens", rr.getPath());
    }

    @Test
    @DisplayName("POST /v1/licenses/tokens/validate -> 200 OK + LicenseValidationResponse (+header)")
    void validateToken_shouldReturn200_andMappedBody_andSendHeader() throws Exception {
        String body = """
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

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        ValidateTokenRequest req = new ValidateTokenRequest()
                .serviceId("crm")
                .serviceVersion("1.5.0")
                .instanceId("crm~host~mac")
                .signature("BASE64SIG");

        String jwt = "jwt-123";
        ApiClientResponse<LicenseValidationResponse> resp = adapter.validateToken(jwt, req);

        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        assertEquals("License is valid", resp.getMessage());
        assertNotNull(resp.getData());
        assertNotNull(resp.getData().getStatus());
        assertEquals("TOKEN_ACTIVE", resp.getData().getStatus().getValue());

        RecordedRequest rr = server.takeRequest();
        assertEquals("POST", rr.getMethod());
        assertEquals("/licensing-service/v1/licenses/tokens/validate", rr.getPath());
        assertEquals(jwt, rr.getHeader("License-Token"));
    }

    @Configuration
    static class TestBeans {
        @Bean RestClient.Builder restClientBuilder() { return RestClient.builder(); }
        @Bean ObjectMapper objectMapper() { return new ObjectMapper(); }
        @Bean ResponseParser responseParser(ObjectMapper om) { return new ResponseParser(om); }
        @Bean ApiClientExecutor apiClientExecutor(ResponseParser rp) { return new ApiClientExecutor(rp); }
    }
}