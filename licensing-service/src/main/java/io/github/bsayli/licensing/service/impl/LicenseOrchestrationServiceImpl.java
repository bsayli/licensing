package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.model.LicenseValidationResult;
import io.github.bsayli.licensing.model.errors.LicenseServiceStatus;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.service.LicenseValidationService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import io.github.bsayli.licensing.service.token.LicenseTokenManager;
import org.springframework.stereotype.Service;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

  private final LicenseValidationService licenseValidationService;
  private final ClientIdGenerator clientIdGenerator;
  private final JwtBlacklistService jwtBlacklistService;
  private final LicenseTokenManager tokenManager;

  public LicenseOrchestrationServiceImpl(
          LicenseValidationService licenseValidationService,
          ClientIdGenerator clientIdGenerator,
          JwtBlacklistService jwtBlacklistService,
          LicenseTokenManager tokenManager) {
    this.licenseValidationService = licenseValidationService;
    this.clientIdGenerator = clientIdGenerator;
    this.jwtBlacklistService = jwtBlacklistService;
    this.tokenManager = tokenManager;
  }

  @Override
  public LicenseValidationResponse issueToken(IssueTokenRequest request) {
    LicenseValidationResult result = licenseValidationService.validateLicense(request);

    String clientId = clientIdGenerator.getClientId(request);
    if (request.forceTokenRefresh()) {
      jwtBlacklistService.addCurrentTokenToBlacklist(clientId);
    }

    String token =
            tokenManager.issueAndCache(
                    clientId,
                    result,
                    request.serviceId(),
                    request.serviceVersion(),
                    request.instanceId(),
                    request.checksum(),
                    request.signature());
    return LicenseValidationResponse.created(token);
  }

  @Override
  public LicenseValidationResponse validateToken(ValidateTokenRequest request, String token) {
    LicenseValidationResult result = licenseValidationService.validateLicense(request, token);

    if (LicenseServiceStatus.TOKEN_REFRESHED == result.serviceStatus()) {
      String clientId = clientIdGenerator.getClientId(request);
      String newToken =
              tokenManager.issueAndCache(
                      clientId,
                      result,
                      request.serviceId(),
                      request.serviceVersion(),
                      request.instanceId(),
                      request.checksum(),
                      request.signature());
      return LicenseValidationResponse.refreshed(newToken);
    }

    return LicenseValidationResponse.active();
  }
}
