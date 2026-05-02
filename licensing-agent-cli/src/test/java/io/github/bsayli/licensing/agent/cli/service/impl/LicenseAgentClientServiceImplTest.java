package io.github.bsayli.licensing.agent.cli.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.github.bsayli.licensing.agent.cli.model.LicenseAgentClientProperties;
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
@DisplayName("Unit Test: LicenseAgentClientServiceImpl")
class LicenseAgentClientServiceImplTest {

  private static LicenseAgentClientProperties props(
      String baseUrl, String apiPath, int connect, int response, int retries, int retryInterval) {
    return new LicenseAgentClientProperties(
        baseUrl, "appUser", "appPass", apiPath, connect, response, retries, retryInterval);
  }

  @Test
  @DisplayName("Success: returns 0 when Agent responds with ServiceResponse and token")
  void validateLicense_success_returnsZero() throws Exception {
    var p = props("http://agent-host:8082/", "/v1/licenses/access", 40, 40, 3, 3);
    var service = spy(new LicenseAgentClientServiceImpl(p));

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
          .thenReturn(
              """
                      {"data":{"licenseToken":"abc123"},"meta":{"serverTime":"2026-05-02T16:00:00Z","sort":[]}}
                      """);

      int rc = service.validateLicense("inst-1", "LK", "crm", "1.0.0");

      assertEquals(0, rc);
      post.verify(() -> Request.post("http://agent-host:8082/v1/licenses/access"));
    }
  }

  @Test
  @DisplayName("URL join: handles base without trailing slash and path without leading slash")
  void validateLicense_urlJoin_withoutSlashes_ok() throws Exception {
    var p = props("http://host:8082", "v1/licenses/access", 40, 40, 0, 1);
    var service = spy(new LicenseAgentClientServiceImpl(p));

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
          .thenReturn(
              """
                      {"data":{"licenseToken":"t"},"meta":{"serverTime":"2026-05-02T16:00:00Z","sort":[]}}
                      """);

      int rc = service.validateLicense("i", "k", "s", "v");

      assertEquals(0, rc);
      post.verify(() -> Request.post("http://host:8082/v1/licenses/access"));
    }
  }

  @Test
  @DisplayName("Failure: returns 1 when success response JSON is invalid")
  void validateLicense_invalidJson_returnsOne() throws Exception {
    var p = props("http://host:8082", "/v1/licenses/access", 40, 40, 0, 1);
    var service = spy(new LicenseAgentClientServiceImpl(p));

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

      assertEquals(1, rc);
    }
  }

  @Test
  @DisplayName("Failure: returns 1 when ServiceResponse has no token")
  void validateLicense_successEnvelopeWithoutToken_returnsOne() throws Exception {
    var p = props("http://host:8082", "/v1/licenses/access", 40, 40, 0, 1);
    var service = spy(new LicenseAgentClientServiceImpl(p));

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
          .thenReturn(
              """
                      {"data":{},"meta":{"serverTime":"2026-05-02T16:00:00Z","sort":[]}}
                      """);

      int rc = service.validateLicense("i", "k", "s", "v");

      assertEquals(1, rc);
    }
  }

  @Test
  @DisplayName("Exception: returns 1 when Agent returns LicenseAgentErrorResponse")
  void validateLicense_agentErrorResponse_returnsOne() throws Exception {
    var p = props("http://host:8082", "/v1/licenses/access", 40, 40, 2, 2);
    var service = spy(new LicenseAgentClientServiceImpl(p));

    try (MockedStatic<Request> post = org.mockito.Mockito.mockStatic(Request.class)) {
      var mockRequest = mock(Request.class, RETURNS_DEEP_STUBS);

      post.when(() -> Request.post(anyString())).thenReturn(mockRequest);

      when(mockRequest.connectTimeout(any())).thenReturn(mockRequest);
      when(mockRequest.responseTimeout(any())).thenReturn(mockRequest);
      when(mockRequest.setHeader(anyString(), anyString())).thenReturn(mockRequest);
      when(mockRequest.bodyString(anyString(), any(ContentType.class))).thenReturn(mockRequest);

      byte[] body =
          """
              {"errorCode":"BAD_REQUEST","message":"Invalid request payload.","errors":null}
              """
              .getBytes(StandardCharsets.UTF_8);

      when(mockRequest.execute(any(CloseableHttpClient.class)))
          .thenThrow(
              new HttpResponseException(400, "Bad Request", body, ContentType.APPLICATION_JSON));

      int rc = service.validateLicense("i", "k", "s", "v");

      assertEquals(1, rc);
    }
  }

  @Test
  @DisplayName("Exception: returns 1 when HttpResponseException has no JSON body")
  void validateLicense_httpExceptionWithoutBody_returnsOne() throws Exception {
    var p = props("http://host:8082", "/v1/licenses/access", 40, 40, 2, 2);
    var service = spy(new LicenseAgentClientServiceImpl(p));

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

      assertEquals(1, rc);
    }
  }
}