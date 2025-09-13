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

  private static final Timeout CONNECT_TIMEOUT = Timeout.of(40, TimeUnit.SECONDS);
  private static final Timeout RESPONSE_TIMEOUT = Timeout.of(40, TimeUnit.SECONDS);
  private static final int RETRIES = 3;

  private final LicenseSdkClientProperties clientProperties;
  private final ObjectMapper mapper = new ObjectMapper();

  public LicenseSdkClientServiceImpl(LicenseSdkClientProperties clientProperties) {
    this.clientProperties = clientProperties;
  }

  @Override
  public Integer validateLicense(String instanceId, String licenseKey, String serviceId, String serviceVersion) {
    final String url = normalizeBaseUrl(clientProperties.baseUrl()) + "/v1/licenses/access";
    final LicenseAccessRequest body = new LicenseAccessRequest(licenseKey, instanceId, null, serviceId, serviceVersion);

    try (CloseableHttpClient http = buildHttpClient()) {
      final String jsonBody = mapper.writeValueAsString(body);
      final String response =
              Request.post(url)
                      .connectTimeout(CONNECT_TIMEOUT)
                      .responseTimeout(RESPONSE_TIMEOUT)
                      .setHeader(HttpHeaders.AUTHORIZATION, basicAuth(clientProperties.appUser(), clientProperties.appPass()))
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


  private CloseableHttpClient buildHttpClient() {
    return HttpClients.custom()
            .setRetryStrategy(new DefaultHttpRequestRetryStrategy(RETRIES, TimeValue.ofSeconds(3)))
            .build();
  }

  private String basicAuth(String user, String pass) {
    final String pair = user + ":" + pass;
    final String b64 = Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    return "Basic " + b64;
  }

  private String normalizeBaseUrl(String url) {
    if (url == null || url.isBlank()) return "";
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  private ApiResponse<LicenseToken> parseApiResponseLicenseToken(String json) {
    try {
      return mapper.readValue(
              json, mapper.getTypeFactory().constructParametricType(ApiResponse.class, LicenseToken.class));
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
      api.errors().forEach(err -> log.error("errorCode={}, message={}", err.errorCode(), err.message()));
    }
  }

  private void logStructuredErrorBodyIfPresent(HttpResponseException e) {
    try {
      byte[] bytes = e.getContentBytes();
      ContentType ct = e.getContentType();
      if (bytes == null || ct == null || !ContentType.APPLICATION_JSON.isSameMimeType(ct)) {
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