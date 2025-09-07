package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.model.LicenseInfo;
import io.github.bsayli.licensing.model.LicenseValidationResult;
import io.github.bsayli.licensing.model.errors.LicenseServiceStatus;
import io.github.bsayli.licensing.model.errors.TokenExpiredException;
import io.github.bsayli.licensing.security.LicenseKeyEncryptor;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.LicenseEvaluationService;
import io.github.bsayli.licensing.service.LicenseValidationService;
import io.github.bsayli.licensing.service.validation.LicenseKeyRequestValidationService;
import io.github.bsayli.licensing.service.validation.LicenseTokenRequestValidationService;
import org.springframework.stereotype.Service;

@Service
public class LicenseValidationServiceImpl implements LicenseValidationService {

  private static final String MSG_LICENSE_KEY_VALID = "license.key.valid";
  private static final String MSG_TOKEN_VALID = "license.token.valid";
  private static final String MSG_TOKEN_REFRESHED = "license.token.refreshed";

  private final LicenseEvaluationService licenseEvaluationService;
  private final LicenseTokenRequestValidationService tokenValidationService;
  private final LicenseKeyRequestValidationService licenseKeyValidationService;
  private final LicenseKeyEncryptor licenseKeyEncryptor;
  private final UserIdEncryptor userIdEncryptor;

  public LicenseValidationServiceImpl(
      LicenseEvaluationService licenseEvaluationService,
      LicenseTokenRequestValidationService tokenValidationService,
      LicenseKeyRequestValidationService licenseKeyValidationService,
      LicenseKeyEncryptor licenseKeyEncryptor,
      UserIdEncryptor userIdEncryptor) {
    this.licenseEvaluationService = licenseEvaluationService;
    this.tokenValidationService = tokenValidationService;
    this.licenseKeyValidationService = licenseKeyValidationService;
    this.licenseKeyEncryptor = licenseKeyEncryptor;
    this.userIdEncryptor = userIdEncryptor;
  }

  @Override
  public LicenseValidationResult validateLicense(IssueTokenRequest request) {
    licenseKeyValidationService.validateSignature(request);

    String decryptedLicenseKey = licenseKeyEncryptor.decrypt(request.licenseKey());
    String userId = userIdEncryptor.extractAndDecryptUserId(decryptedLicenseKey);

    if (!request.forceTokenRefresh()) {
      licenseKeyValidationService.assertNotCachedWithSameContext(request, userId);
    }

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
  public LicenseValidationResult validateLicense(ValidateTokenRequest request, String token) {
    try {
      tokenValidationService.validateTokenRequest(request, token);

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
          .serviceStatus(LicenseServiceStatus.TOKEN_REFRESHED)
          .message(MSG_TOKEN_REFRESHED)
          .build();
    }
  }
}
