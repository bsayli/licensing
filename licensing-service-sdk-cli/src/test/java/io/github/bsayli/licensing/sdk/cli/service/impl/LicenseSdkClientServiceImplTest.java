package io.github.bsayli.licensing.sdk.cli.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.github.bsayli.licensing.sdk.cli.model.LicenseSdkClientProperties;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseSdkClientServiceImpl")
class LicenseSdkClientServiceImplTest {

    private static LicenseSdkClientProperties props(
            String baseUrl, String apiPath, int connect, int response, int retries, int retryInterval) {
        return new LicenseSdkClientProperties(
                baseUrl,            // baseUrl
                "appUser",          // appUser
                "appPass",          // appPass
                apiPath,            // apiPath
                connect,            // connectTimeoutSeconds
                response,           // responseTimeoutSeconds
                retries,            // retries
                retryInterval       // retryIntervalSeconds
        );
    }

    @Test
    @DisplayName("Success: returns 0 when API responds with status=200 and token")
    void validateLicense_success_returnsZero() throws Exception {
        // base URL ends with '/', path starts with '/' -> joinUrl should not double-slash
        var p = props("http://sdk-host:8082/", "/v1/licenses/access", 40, 40, 3, 3);
        var service = spy(new LicenseSdkClientServiceImpl(p));

        try (MockedStatic<Request> post = org.mockito.Mockito.mockStatic(Request.class)) {
            var mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);
            var mockResponse = mock(Response.class);
            var mockContent = mock(Content.class);

            post.when(() -> Request.post(anyString())).thenReturn(mockRequest);

            when(mockRequest.connectTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.responseTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.setHeader(anyString(), anyString())).thenReturn(mockRequest);
            when(mockRequest.bodyString(anyString(), any(ContentType.class))).thenReturn(mockRequest);
            when(mockRequest.execute(any(CloseableHttpClient.class))).thenReturn(mockResponse);
            when(mockResponse.returnContent()).thenReturn(mockContent);

            String json = """
          {"status":200,"data":{"licenseToken":"abc123"},"message":"valid"}
          """;
            when(mockContent.asString(StandardCharsets.UTF_8)).thenReturn(json);

            int rc = service.validateLicense("inst-1", "LK", "crm", "1.0.0");
            assertEquals(0, rc, "Expected success (0) when API returns status=200 with token");

            // URL kontrolü
            post.verify(() -> Request.post("http://sdk-host:8082/v1/licenses/access"));
        }
    }

    @Test
    @DisplayName("URL join: handles base without trailing slash and path without leading slash")
    void validateLicense_urlJoin_withoutSlashes_ok() throws Exception {
        // base without '/', path without '/' -> joinUrl should add exactly one '/'
        var p = props("http://host:8082", "v1/licenses/access", 40, 40, 0, 1);
        var service = spy(new LicenseSdkClientServiceImpl(p));

        try (MockedStatic<Request> post = org.mockito.Mockito.mockStatic(Request.class)) {
            var mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);
            var mockResponse = mock(Response.class);
            var mockContent = mock(Content.class);

            post.when(() -> Request.post(anyString())).thenReturn(mockRequest);

            when(mockRequest.connectTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.responseTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.setHeader(anyString(), anyString())).thenReturn(mockRequest);
            when(mockRequest.bodyString(anyString(), any(ContentType.class))).thenReturn(mockRequest);
            when(mockRequest.execute(any(CloseableHttpClient.class))).thenReturn(mockResponse);
            when(mockResponse.returnContent()).thenReturn(mockContent);

            when(mockContent.asString(StandardCharsets.UTF_8))
                    .thenReturn("""
            {"status":200,"data":{"licenseToken":"t"} }
            """);

            int rc = service.validateLicense("i", "k", "s", "v");
            assertEquals(0, rc);

            post.verify(() -> Request.post("http://host:8082/v1/licenses/access"));
        }
    }

    @Test
    @DisplayName("Failure: returns 1 when JSON is invalid (parse fails)")
    void validateLicense_invalidJson_returnsOne() throws Exception {
        var p = props("http://host:8082", "/v1/licenses/access", 40, 40, 0, 1);
        var service = spy(new LicenseSdkClientServiceImpl(p));

        try (MockedStatic<Request> post = org.mockito.Mockito.mockStatic(Request.class)) {
            var mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);
            var mockResponse = mock(Response.class);
            var mockContent = mock(Content.class);

            post.when(() -> Request.post(anyString())).thenReturn(mockRequest);

            when(mockRequest.connectTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.responseTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.setHeader(anyString(), anyString())).thenReturn(mockRequest);
            when(mockRequest.bodyString(anyString(), any(ContentType.class))).thenReturn(mockRequest);
            when(mockRequest.execute(any(CloseableHttpClient.class))).thenReturn(mockResponse);
            when(mockResponse.returnContent()).thenReturn(mockContent);

            when(mockContent.asString(StandardCharsets.UTF_8)).thenReturn("not-json");

            int rc = service.validateLicense("i", "k", "s", "v");
            assertEquals(1, rc, "Invalid JSON should return 1");
        }
    }

    @Test
    @DisplayName("Failure: returns 1 when API returns non-200 response JSON")
    void validateLicense_non200_returnsOne() throws Exception {
        var p = props("http://host:8082", "/v1/licenses/access", 40, 40, 0, 1);
        var service = spy(new LicenseSdkClientServiceImpl(p));

        try (MockedStatic<Request> post = org.mockito.Mockito.mockStatic(Request.class)) {
            var mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);
            var mockResponse = mock(Response.class);
            var mockContent = mock(Content.class);

            post.when(() -> Request.post(anyString())).thenReturn(mockRequest);

            when(mockRequest.connectTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.responseTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.setHeader(anyString(), anyString())).thenReturn(mockRequest);
            when(mockRequest.bodyString(anyString(), any(ContentType.class))).thenReturn(mockRequest);
            when(mockRequest.execute(any(CloseableHttpClient.class))).thenReturn(mockResponse);
            when(mockResponse.returnContent()).thenReturn(mockContent);

            String json = """
          {"status":400,"message":"bad request",
           "errors":[{"errorCode":"E400","message":"invalid"}]}
          """;
            when(mockContent.asString(StandardCharsets.UTF_8)).thenReturn(json);

            int rc = service.validateLicense("i", "k", "s", "v");
            assertEquals(1, rc, "Non-200 response should return 1");
        }
    }

    @Test
    @DisplayName("Exception: returns 1 when HttpResponseException is thrown")
    void validateLicense_httpException_returnsOne() throws Exception {
        var p = props("http://host:8082", "/v1/licenses/access", 40, 40, 2, 2);
        var service = spy(new LicenseSdkClientServiceImpl(p));

        try (MockedStatic<Request> post = org.mockito.Mockito.mockStatic(Request.class)) {
            var mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);

            post.when(() -> Request.post(anyString())).thenReturn(mockRequest);

            when(mockRequest.connectTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.responseTimeout(any())).thenReturn(mockRequest);
            when(mockRequest.setHeader(anyString(), anyString())).thenReturn(mockRequest);
            when(mockRequest.bodyString(anyString(), any(ContentType.class))).thenReturn(mockRequest);
            when(mockRequest.execute(any(CloseableHttpClient.class)))
                    .thenThrow(new HttpResponseException(500, "Internal error"));

            int rc = service.validateLicense("i", "k", "s", "v");
            assertEquals(1, rc, "HttpResponseException should result in 1");
        }
    }
}