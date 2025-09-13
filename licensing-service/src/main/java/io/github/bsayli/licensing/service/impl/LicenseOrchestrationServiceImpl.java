package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import io.github.bsayli.licensing.service.LicenseValidationService;
import io.github.bsayli.licensing.service.token.LicenseTokenIssueRequest;
import io.github.bsayli.licensing.service.token.LicenseTokenManager;
import org.springframework.stereotype.Service;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

  private final LicenseValidationService licenseValidationService;
  private final ClientIdGenerator clientIdGenerator;
  private final LicenseTokenManager tokenManager;

  public LicenseOrchestrationServiceImpl(
      LicenseValidationService licenseValidationService,
      ClientIdGenerator clientIdGenerator,
      LicenseTokenManager tokenManager) {
    this.licenseValidationService = licenseValidationService;
    this.clientIdGenerator = clientIdGenerator;
    this.tokenManager = tokenManager;
  }

  @Override
  public LicenseAccessResponse issueAccess(IssueAccessRequest request) {
    LicenseValidationResult result = licenseValidationService.validateLicense(request);
    String clientId = clientIdGenerator.getClientId(request);
    String existing = tokenManager.peekActive(clientId);
    if (existing != null) {
      return LicenseAccessResponse.active(existing);
    }

    String token =
        tokenManager.issueAndCache(
            new LicenseTokenIssueRequest.Builder()
                .clientId(clientId)
                .result(result)
                .serviceId(request.serviceId())
                .serviceVersion(request.serviceVersion())
                .instanceId(request.instanceId())
                .checksum(request.checksum())
                .signature(request.signature())
                .build());

    return LicenseAccessResponse.created(token);
  }

  @Override
  public LicenseAccessResponse validateAccess(ValidateAccessRequest request, String token) {
    LicenseValidationResult result = licenseValidationService.validateLicense(request, token);

    if (ServiceErrorCode.TOKEN_REFRESHED == result.serviceStatus()) {
      String clientId = clientIdGenerator.getClientId(request);
      String newToken =
          tokenManager.issueAndCache(
              new LicenseTokenIssueRequest.Builder()
                  .clientId(clientId)
                  .result(result)
                  .serviceId(request.serviceId())
                  .serviceVersion(request.serviceVersion())
                  .instanceId(request.instanceId())
                  .checksum(request.checksum())
                  .signature(request.signature())
                  .build());
      return LicenseAccessResponse.refreshed(newToken);
    }

    return LicenseAccessResponse.active();
  }
}
