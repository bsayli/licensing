package com.c9.licensing.sdk.cli.service.impl;

import com.c9.licensing.sdk.cli.model.LicenseSdkClientProperties;
import com.c9.licensing.sdk.cli.model.LicenseValidationResponse;
import com.c9.licensing.sdk.cli.service.LicenseSdkClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseSdkClientServiceImpl implements LicenseSdkClientService {

  private static final Logger logger = LoggerFactory.getLogger(LicenseSdkClientServiceImpl.class);

  private final LicenseSdkClientProperties clientProperties;

  public LicenseSdkClientServiceImpl(LicenseSdkClientProperties clientProperties) {
    this.clientProperties = clientProperties;
  }

  @Override
  public Integer validateLicense(
      String instanceId, String licenseKey, String serviceId, String serviceVersion) {
    String endpointUrl = clientProperties.baseUrl() + "/api/license/validate";
    CloseableHttpClient httpClient = buildHttpClient();
    Request postRequest =
        buildPostRequest(endpointUrl, instanceId, licenseKey, serviceId, serviceVersion);
    return callAndHandleResponse(httpClient, postRequest);
  }

  private Integer callAndHandleResponse(CloseableHttpClient httpClient, Request postRequest) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      String response = postRequest.execute(httpClient).returnContent().toString();
      LicenseValidationResponse licenseResponse =
          mapper.readValue(response, LicenseValidationResponse.class);
      String licenseResponsePrettyStr =
          mapper.writerWithDefaultPrettyPrinter().writeValueAsString(licenseResponse);
      logger.info("License validated successfully:");
      logger.info(licenseResponsePrettyStr);
      return 0;
    } catch (HttpResponseException e) {
      int statusCode = e.getStatusCode();
      if (isServerError(statusCode, e.getContentType())) {
        LicenseValidationResponse errorResponse = parseErrorResponse(e, mapper);
        String licenseErrorResponsePrettyStr = getPrettyJsonStr(mapper, errorResponse);
        logger.error("License validatation failed with status code {}", statusCode);
        logger.error(licenseErrorResponsePrettyStr);
        return 1;
      } else {
        logger.error("License validatation failed with status code {}", statusCode);
        return 1;
      }
    } catch (Exception e) {
      logger.error("An unexpected error occured:", e);
      return 1;
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private CloseableHttpClient buildHttpClient() {
    return HttpClients.custom()
        .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.of(3, TimeUnit.SECONDS)))
        .build();
  }

  private Request buildPostRequest(
      String endpointUrl,
      String instanceId,
      String licenseKey,
      String serviceId,
      String serviceVersion) {
    return Request.post(endpointUrl)
        .connectTimeout(Timeout.of(40, TimeUnit.SECONDS))
        .responseTimeout(Timeout.of(40, TimeUnit.SECONDS))
        .setHeader(
            HttpHeaders.AUTHORIZATION,
            createBasicAuthHeader(clientProperties.appUser(), clientProperties.appPass()))
        .addHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString())
        .addHeader("Accept", ContentType.APPLICATION_JSON.toString())
        .addHeader("X-Instance-ID", instanceId)
        .bodyForm(
            new BasicNameValuePair("licenseKey", licenseKey),
            new BasicNameValuePair("serviceId", serviceId),
            new BasicNameValuePair("serviceVersion", serviceVersion));
  }

  private String createBasicAuthHeader(String appUser, String appPass) {
    String authString = appUser + ":" + appPass;
    byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
    return "Basic " + new String(authEncBytes);
  }

  private boolean isServerError(int statusCode, ContentType contentType) {
    List<Integer> validStatusCodes =
        List.of(
            HttpStatus.SC_BAD_REQUEST,
            HttpStatus.SC_UNAUTHORIZED,
            HttpStatus.SC_INTERNAL_SERVER_ERROR);
    return validStatusCodes.contains(statusCode)
        && ContentType.APPLICATION_JSON.isSameMimeType(contentType);
  }

  private LicenseValidationResponse parseErrorResponse(
      HttpResponseException e, ObjectMapper mapper) {
    try {
      return mapper.readValue(e.getContentBytes(), LicenseValidationResponse.class);
    } catch (IOException e1) {
      e1.printStackTrace();
      return null;
    }
  }

  private String getPrettyJsonStr(ObjectMapper mapper, LicenseValidationResponse errorResponse) {
    if (errorResponse == null) return "";
    String licenseErrorResponsePrettyStr = "";
    try {
      licenseErrorResponsePrettyStr =
          mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResponse);
    } catch (JsonProcessingException e1) {
      e1.printStackTrace();
    }
    return licenseErrorResponsePrettyStr;
  }
}
