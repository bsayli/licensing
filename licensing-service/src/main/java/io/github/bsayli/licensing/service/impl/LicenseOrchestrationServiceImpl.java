package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.model.ClientInfo;
import io.github.bsayli.licensing.model.LicenseServiceStatus;
import io.github.bsayli.licensing.model.LicenseValidationResult;
import io.github.bsayli.licensing.model.errors.InvalidParameterException;
import io.github.bsayli.licensing.service.LicenseClientCacheManagementService;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.service.LicenseService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import io.github.bsayli.licensing.service.jwt.JwtService;
import org.springframework.stereotype.Service;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

  private final LicenseService licenseService;
  private final JwtService jwtUtil;
  private final LicenseClientCacheManagementService clientCacheManagementService;
  private final ClientIdGenerator clientIdGenerator;
  private final JwtBlacklistService jwtBlacklistService;

  public LicenseOrchestrationServiceImpl(
      LicenseService licenseService,
      JwtService jwtUtil,
      LicenseClientCacheManagementService clientCacheManagementService,
      ClientIdGenerator clientIdGenerator,
      JwtBlacklistService jwtBlacklistService) {
    this.licenseService = licenseService;
    this.jwtUtil = jwtUtil;
    this.clientCacheManagementService = clientCacheManagementService;
    this.clientIdGenerator = clientIdGenerator;
    this.jwtBlacklistService = jwtBlacklistService;
  }

  @Override
  public LicenseValidationResponse getLicenseDetails(LicenseValidationRequest request) {

    validateParameterPresence(request.licenseKey(), request.licenseToken());

    if (request.licenseKey() != null) {
      return getLicenseDetailsByLicenseKey(request);
    } else {
      return getLicenseDetailsByToken(request);
    }
  }

  @Override
  public LicenseValidationResponse getLicenseDetailsByLicenseKey(LicenseValidationRequest request) {
    LicenseValidationResponse licenseValidationResponse;
    LicenseValidationResult result = licenseService.getUserLicenseDetailsByLicenseKey(request);
    if (result.valid()) {

      String clientId = clientIdGenerator.getClientId(request);

      if (request.forceTokenRefresh()) {
        jwtBlacklistService.addCurrentTokenToBlacklist(clientId);
      }

      String token = generateTokenAndAddToCache(clientId, request, result);

      licenseValidationResponse =
          new LicenseValidationResponse.Builder()
              .success(true)
              .licenseToken(token)
              .status(LicenseServiceStatus.TOKEN_CREATED.name())
              .message(result.message())
              .build();
    } else {
      licenseValidationResponse =
          new LicenseValidationResponse.Builder()
              .success(false)
              .status(result.serviceStatus().name())
              .message(result.message())
              .build();
    }

    return licenseValidationResponse;
  }

  @Override
  public LicenseValidationResponse getLicenseDetailsByToken(LicenseValidationRequest request) {
    LicenseValidationResponse licenseValidationResponse;
    LicenseValidationResult result = licenseService.getUserLicenseDetailsByToken(request);
    if (result.valid()) {
      if (LicenseServiceStatus.TOKEN_REFRESHED == result.serviceStatus()) {
        String clientId = clientIdGenerator.getClientId(request);
        String newToken = generateTokenAndAddToCache(clientId, request, result);
        licenseValidationResponse =
            new LicenseValidationResponse.Builder()
                .success(true)
                .licenseToken(newToken)
                .status(result.serviceStatus().name())
                .message(result.message())
                .build();
      } else {
        licenseValidationResponse =
            new LicenseValidationResponse.Builder()
                .success(true)
                .licenseToken(request.licenseToken())
                .status(LicenseServiceStatus.TOKEN_ACTIVE.name())
                .message(result.message())
                .build();
      }

    } else {
      licenseValidationResponse =
          new LicenseValidationResponse.Builder()
              .success(false)
              .status(result.serviceStatus().name())
              .message(result.message())
              .build();
    }
    return licenseValidationResponse;
  }

  private void validateParameterPresence(String licenseKey, String licenseToken) {
    if (licenseKey == null && licenseToken == null) {
      throw new InvalidParameterException("Either licenseKey or licenseToken is required");
    }
    if (licenseKey != null && licenseToken != null) {
      throw new InvalidParameterException("Only one of licenseKey or licenseToken can be provided");
    }
  }

  private String generateTokenAndAddToCache(
      String clientId, LicenseValidationRequest request, LicenseValidationResult result) {
    String token = jwtUtil.generateToken(clientId, result.licenseTier(), result.licenseStatus());
    ClientInfo clientInfo =
        new ClientInfo.Builder()
            .serviceId(request.serviceId())
            .licenseToken(token)
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId())
            .encUserId(result.userId())
            .checksum(request.checksum())
            .signature(request.signature())
            .build();
    clientCacheManagementService.addClientInfo(clientInfo);
    return token;
  }
}
