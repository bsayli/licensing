package com.c9.licensing.sdk.service.impl;

import static com.c9.licensing.sdk.model.server.LicenseServerServiceStatus.TOKEN_ALREADY_EXIST;
import static com.c9.licensing.sdk.model.server.LicenseServerServiceStatus.TOKEN_IS_TOO_OLD_FOR_REFRESH;

import com.c9.licensing.sdk.exception.LicenseServiceClientErrorException;
import com.c9.licensing.sdk.exception.LicenseServiceServerErrorException;
import com.c9.licensing.sdk.exception.LicenseServiceUnhandledErrorException;
import com.c9.licensing.sdk.exception.TokenAlreadyExistException;
import com.c9.licensing.sdk.exception.TokenIsTooOldForRefreshException;
import com.c9.licensing.sdk.generator.SignatureGenerator;
import com.c9.licensing.sdk.model.server.LicenseServerValidationRequest;
import com.c9.licensing.sdk.model.server.LicenseServerValidationResponse;
import com.c9.licensing.sdk.service.LicenseService;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class LicenseServiceImpl implements LicenseService {

  private final RestClient licensingRestClient;
  private final SignatureGenerator signatureGenerator;
  private final MappingJackson2HttpMessageConverter httpMessageConverter;

  public LicenseServiceImpl(
      RestClient licensingRestClient,
      SignatureGenerator signatureGenerator,
      MappingJackson2HttpMessageConverter httpMessageConverter) {
    this.licensingRestClient = licensingRestClient;
    this.signatureGenerator = signatureGenerator;
    this.httpMessageConverter = httpMessageConverter;
  }

  public LicenseServerValidationResponse getLicenseDetails(LicenseServerValidationRequest request) {
    LicenseServerValidationResponse response = null;
    try {
      response = getLicense(request);
    } catch (TokenIsTooOldForRefreshException e) {
      LicenseServerValidationRequest newTokenRequest =
          new LicenseServerValidationRequest.Builder()
              .serviceId(request.serviceId())
              .serviceVersion(request.serviceVersion())
              .instanceId(request.instanceId())
              .licenseKey(request.licenseKey())
              .checksum(request.checksum())
              .build();
      response = getLicense(newTokenRequest);
    } catch (TokenAlreadyExistException e) {
      LicenseServerValidationRequest forceTokenRefreshRequest =
          new LicenseServerValidationRequest.Builder()
              .serviceId(request.serviceId())
              .serviceVersion(request.serviceVersion())
              .instanceId(request.instanceId())
              .licenseKey(request.licenseKey())
              .checksum(request.checksum())
              .forceTokenRefresh(true)
              .build();
      response = getLicense(forceTokenRefreshRequest);
    }

    return response;
  }

  private LicenseServerValidationResponse getLicense(LicenseServerValidationRequest request) {
    MultiValueMap<String, String> requestParams = getRequestParams(request);

    String signature = signatureGenerator.generateSignature(request);
    HttpHeaders requestHeaders = getRequestHeaders(request, signature);

    return licensingRestClient
        .post()
        .uri(URI_LICENSE_VALIDATE)
        .headers(h -> h.addAll(requestHeaders))
        .body(requestParams)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
        .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
        .body(LicenseServerValidationResponse.class);
  }

  private void handleClientError(HttpRequest request, ClientHttpResponse response) {
    if (response != null) {
      try {
        if (HttpStatus.UNAUTHORIZED.isSameCodeAs(response.getStatusCode())) {
          LicenseServerValidationResponse serverResponse =
              (LicenseServerValidationResponse)
                  httpMessageConverter.read(LicenseServerValidationResponse.class, response);
          String status = serverResponse.status();
          if (TOKEN_IS_TOO_OLD_FOR_REFRESH.name().equals(status)) {
            throw new TokenIsTooOldForRefreshException("Token is too old for refresh");
          } else if (TOKEN_ALREADY_EXIST.name().equals(status)) {
            throw new TokenAlreadyExistException("Token is already exist");
          }
          throw new LicenseServiceClientErrorException(
              serverResponse, "License service client error message");
        }
      } catch (HttpMessageNotReadableException | IOException e) {
        throw new LicenseServiceUnhandledErrorException(
            MESSAGE_LICENSE_SERVICE_CLIENT_UNHANDLED_ERROR);
      }
    }
    throw new LicenseServiceUnhandledErrorException(MESSAGE_LICENSE_SERVICE_CLIENT_UNHANDLED_ERROR);
  }

  private void handleServerError(HttpRequest request, ClientHttpResponse response) {
    if (response != null) {
      try {
        if (HttpStatus.INTERNAL_SERVER_ERROR.isSameCodeAs(response.getStatusCode())) {
          LicenseServerValidationResponse serverResponse =
              (LicenseServerValidationResponse)
                  httpMessageConverter.read(LicenseServerValidationResponse.class, response);
          throw new LicenseServiceServerErrorException(
              serverResponse, "License service server error message");
        }
      } catch (HttpMessageNotReadableException | IOException e) {
        throw new LicenseServiceUnhandledErrorException(
            MESSAGE_LICENSE_SERVICE_SERVER_UNHANDLED_ERROR);
      }
    }
    throw new LicenseServiceUnhandledErrorException(MESSAGE_LICENSE_SERVICE_SERVER_UNHANDLED_ERROR);
  }

  private MultiValueMap<String, String> getRequestParams(LicenseServerValidationRequest request) {
    MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
    requestParams.add(REQUEST_PARAM_SERVICE_ID, request.serviceId());
    requestParams.add(REQUEST_PARAM_SERVICE_VERSION, request.serviceVersion());

    if (request.licenseToken() == null) {
      requestParams.add(REQUEST_PARAM_LICENSE_KEY, request.licenseKey());
    }

    return requestParams;
  }

  private HttpHeaders getRequestHeaders(LicenseServerValidationRequest request, String signature) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
    httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
    if (request.licenseToken() != null) {
      httpHeaders.add(HEADER_PARAM_LICENSE_TOKEN, request.licenseToken());
    }

    httpHeaders.add(HEADER_PARAM_INSTANCE_ID, request.instanceId());

    String checksum = request.checksum();
    if (checksum != null && !checksum.trim().isEmpty()) {
      httpHeaders.add(HEADER_PARAM_CHECKSUM, checksum);
    }

    if (signature != null && !signature.trim().isEmpty()) {
      httpHeaders.add(HEADER_PARAM_SIGNATURE, signature);
    }

    boolean forceTokenRefresh = request.forceTokenRefresh();
    if (forceTokenRefresh) {
      httpHeaders.add(HEADER_PARAM_FORCE_TOKEN_REFRESH, Boolean.toString(forceTokenRefresh));
    }

    return httpHeaders;
  }
}
