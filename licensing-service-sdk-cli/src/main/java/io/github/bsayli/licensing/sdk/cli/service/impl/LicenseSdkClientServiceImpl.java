package io.github.bsayli.licensing.sdk.cli.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.sdk.cli.model.ApiResponse;
import io.github.bsayli.licensing.sdk.cli.model.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.cli.model.LicenseSdkClientProperties;
import io.github.bsayli.licensing.sdk.cli.model.LicenseToken;
import io.github.bsayli.licensing.sdk.cli.service.LicenseSdkClientService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseSdkClientServiceImpl implements LicenseSdkClientService {

  private static final Logger log = LoggerFactory.getLogger(LicenseSdkClientServiceImpl.class);

  private final LicenseSdkClientProperties props;
  private final ObjectMapper mapper = new ObjectMapper();

  public LicenseSdkClientServiceImpl(LicenseSdkClientProperties clientProperties) {
    this.props = clientProperties;
  }

  @Override
  public Integer validateLicense(
      String instanceId, String licenseKey, String serviceId, String serviceVersion) {

    final String url = joinUrl(props.baseUrl(), props.apiPath());

    final LicenseAccessRequest body =
        new LicenseAccessRequest(licenseKey, instanceId, null, serviceId, serviceVersion);

    final Timeout connectTimeout =
        Timeout.of(Math.max(1, props.connectTimeoutSeconds()), TimeUnit.SECONDS);
    final Timeout responseTimeout =
        Timeout.of(Math.max(1, props.responseTimeoutSeconds()), TimeUnit.SECONDS);

    try (CloseableHttpClient http =
        buildHttpClient(props.retries(), props.retryIntervalSeconds())) {

      final String jsonBody = mapper.writeValueAsString(body);

      final String response =
          Request.post(url)
              .connectTimeout(connectTimeout)
              .responseTimeout(responseTimeout)
              .setHeader(HttpHeaders.AUTHORIZATION, basicAuth(props.appUser(), props.appPass()))
              .setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
              .bodyString(jsonBody, ContentType.APPLICATION_JSON)
              .execute(http)
              .returnContent()
              .asString(StandardCharsets.UTF_8);

      final ApiResponse<LicenseToken> api = parseApiResponseLicenseToken(response);
      if (api == null) {
        log.error("License validation failed. Response could not be parsed as ApiResponse.");
        return 1;
      }
      if (isSuccess(api)) {
        logSuccess(api);
        return 0;
      }
      logFailure(api);
      return 1;

    } catch (HttpResponseException e) {
      log.error("HTTP error: status={}, reason={}", e.getStatusCode(), e.getReasonPhrase());
      logStructuredErrorBodyIfPresent(e);
      return 1;

    } catch (Exception e) {
      log.error("Unexpected error while calling SDK service", e);
      return 1;
    }
  }

  private CloseableHttpClient buildHttpClient(int retries, int retryIntervalSeconds) {
    int safeRetries = Math.max(0, retries);
    int safeInterval = Math.max(1, retryIntervalSeconds);
    return HttpClients.custom()
        .setRetryStrategy(
            new DefaultHttpRequestRetryStrategy(safeRetries, TimeValue.ofSeconds(safeInterval)))
        .build();
  }

  private String basicAuth(String user, String pass) {
    final String pair = user + ":" + pass;
    final String b64 = Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    return "Basic " + b64;
  }

  private String joinUrl(String baseUrl, String path) {
    if (baseUrl == null || baseUrl.isBlank()) return path == null ? "" : path;
    if (path == null || path.isBlank()) return baseUrl;

    String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    String p = path.startsWith("/") ? path : "/" + path;
    return base + p;
  }

  private ApiResponse<LicenseToken> parseApiResponseLicenseToken(String json) {
    try {
      return mapper.readValue(
          json,
          mapper.getTypeFactory().constructParametricType(ApiResponse.class, LicenseToken.class));
    } catch (Exception e) {
      log.error("Failed to parse ApiResponse JSON", e);
      return null;
    }
  }

  private boolean isSuccess(ApiResponse<LicenseToken> api) {
    return api.status() == 200 && api.data() != null && api.data().licenseToken() != null;
  }

  private void logSuccess(ApiResponse<LicenseToken> api) {
    log.info("License validated successfully.");
    log.info("Token: {}", api.data().licenseToken());
    if (api.message() != null && !api.message().isBlank()) {
      log.info("Message: {}", api.message());
    }
  }

  private void logFailure(ApiResponse<LicenseToken> api) {
    log.error("License validation failed. status={}, message={}", api.status(), api.message());
    if (api.errors() != null) {
      api.errors()
          .forEach(err -> log.error("errorCode={}, message={}", err.errorCode(), err.message()));
    }
  }

  private void logStructuredErrorBodyIfPresent(HttpResponseException e) {
    try {
      byte[] bytes = e.getContentBytes();
      ContentType ct = e.getContentType();
      if (bytes == null || !ContentType.APPLICATION_JSON.isSameMimeType(ct)) {
        log.error("No JSON error body available.");
        return;
      }
      final String body = new String(bytes, StandardCharsets.UTF_8);
      final ApiResponse<LicenseToken> api = parseApiResponseLicenseToken(body);
      if (api == null) {
        log.error("Error body is not a valid ApiResponse JSON.");
        return;
      }
      logFailure(api);
    } catch (Exception parseErr) {
      log.error("Failed to parse error body", parseErr);
    }
  }
}
