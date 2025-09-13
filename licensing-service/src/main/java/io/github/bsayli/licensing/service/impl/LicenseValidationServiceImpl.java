package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.LicenseEvaluationService;
import io.github.bsayli.licensing.service.LicenseValidationService;
import io.github.bsayli.licensing.service.exception.token.TokenExpiredException;
import io.github.bsayli.licensing.service.validation.LicenseKeyRequestValidator;
import io.github.bsayli.licensing.service.validation.TokenRequestValidator;
import org.springframework.stereotype.Service;

@Service
public class LicenseValidationServiceImpl implements LicenseValidationService {

  private static final String MSG_LICENSE_KEY_VALID = "license.key.valid";
  private static final String MSG_TOKEN_VALID = "license.token.valid";
  private static final String MSG_TOKEN_REFRESHED = "license.token.refreshed";

  private final LicenseEvaluationService licenseEvaluationService;
  private final TokenRequestValidator tokenValidationService;
  private final LicenseKeyRequestValidator licenseKeyValidationService;
  private final UserIdEncryptor userIdEncryptor;

  public LicenseValidationServiceImpl(
      LicenseEvaluationService licenseEvaluationService,
      TokenRequestValidator tokenValidationService,
      LicenseKeyRequestValidator licenseKeyValidationService,
      UserIdEncryptor userIdEncryptor) {
    this.licenseEvaluationService = licenseEvaluationService;
    this.tokenValidationService = tokenValidationService;
    this.licenseKeyValidationService = licenseKeyValidationService;
    this.userIdEncryptor = userIdEncryptor;
  }

  @Override
  public LicenseValidationResult validateLicense(IssueAccessRequest request) {
    licenseKeyValidationService.assertSignatureValid(request);

    String userId = userIdEncryptor.extractAndDecryptUserId(request.licenseKey());

    licenseKeyValidationService.assertNoConflictingCachedContext(request, userId);

    LicenseInfo info = licenseEvaluationService.evaluateLicense(request, userId);

    return new LicenseValidationResult.Builder()
        .valid(true)
        .userId(userIdEncryptor.encrypt(info.userId()))
        .appInstanceId(request.instanceId())
        .licenseStatus(info.licenseStatus())
        .licenseTier(info.licenseTier())
        .message(MSG_LICENSE_KEY_VALID)
        .build();
  }

  @Override
  public LicenseValidationResult validateLicense(ValidateAccessRequest request, String token) {
    try {
      tokenValidationService.assertValid(request, token);
      return new LicenseValidationResult.Builder().valid(true).message(MSG_TOKEN_VALID).build();

    } catch (TokenExpiredException e) {
      String userId = userIdEncryptor.decrypt(e.getEncUserId());
      LicenseInfo info = licenseEvaluationService.evaluateLicense(request, userId);

      return new LicenseValidationResult.Builder()
          .valid(true)
          .userId(userIdEncryptor.encrypt(info.userId()))
          .appInstanceId(request.instanceId())
          .licenseStatus(info.licenseStatus())
          .licenseTier(info.licenseTier())
          .serviceStatus(ServiceErrorCode.TOKEN_REFRESHED)
          .message(MSG_TOKEN_REFRESHED)
          .build();
    }
  }
}
