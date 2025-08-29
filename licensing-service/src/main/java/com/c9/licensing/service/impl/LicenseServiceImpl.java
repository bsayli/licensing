package com.c9.licensing.service.impl;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseServiceStatus;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.model.errors.LicenseServiceExceptionImpl;
import com.c9.licensing.model.errors.TokenAlreadyExistException;
import com.c9.licensing.model.errors.TokenExpiredException;
import com.c9.licensing.security.LicenseKeyEncryptor;
import com.c9.licensing.security.UserIdEncryptor;
import com.c9.licensing.service.LicenseDetailsService;
import com.c9.licensing.service.LicenseService;
import com.c9.licensing.service.validation.LicenseKeyRequestValidationService;
import com.c9.licensing.service.validation.LicenseTokenRequestValidationService;
import org.springframework.stereotype.Service;

@Service
public class LicenseServiceImpl implements LicenseService {

  private final LicenseDetailsService licenseDetailsService;
  private final LicenseTokenRequestValidationService tokenValidationService;
  private final LicenseKeyRequestValidationService licenseKeyValidationService;
  private final LicenseKeyEncryptor licenseKeyEncryptor;
  private final UserIdEncryptor userIdEncryptor;

  public LicenseServiceImpl(
      LicenseDetailsService licenseDetailsService,
      LicenseTokenRequestValidationService tokenValidationService,
      LicenseKeyRequestValidationService licenseKeyValidationService,
      LicenseKeyEncryptor licenseKeyEncryptor,
      UserIdEncryptor userIdEncryptor) {
    this.licenseDetailsService = licenseDetailsService;
    this.tokenValidationService = tokenValidationService;
    this.licenseKeyValidationService = licenseKeyValidationService;
    this.userIdEncryptor = userIdEncryptor;
    this.licenseKeyEncryptor = licenseKeyEncryptor;
  }

  public LicenseValidationResult getUserLicenseDetailsByLicenseKey(
      LicenseValidationRequest request) {
    LicenseValidationResult validationResult;
    try {
      licenseKeyValidationService.checkSignature(request);

      String licenseKey = licenseKeyEncryptor.decrypt(request.licenseKey());
      String userId = userIdEncryptor.extractAndDecryptUserId(licenseKey);
      if (!request.forceTokenRefresh()) {
        licenseKeyValidationService.checkLicenseKeyRequestWithCachedData(request, userId);
      }

      LicenseInfo info = licenseDetailsService.getAndValidateLicenseDetails(request, userId);
      validationResult =
          getValidationResult(request.instanceId(), info, null, LICENSE_KEY_IS_VALID);

    } catch (TokenAlreadyExistException e) {
      validationResult =
          new LicenseValidationResult.Builder()
              .valid(false)
              .serviceStatus(e.getStatus())
              .message(e.getMessage())
              .build();

    } catch (LicenseServiceExceptionImpl e) {
      logger.error(LICENSE_VALIDATION_FAILED, e);
      validationResult =
          new LicenseValidationResult.Builder()
              .valid(false)
              .serviceStatus(e.getStatus())
              .message(e.getMessage())
              .build();

    } catch (Exception e) {
      logger.error(ERROR_DURING_LICENSE_VALIDATION, e);
      validationResult =
          new LicenseValidationResult.Builder()
              .valid(false)
              .serviceStatus(LicenseServiceStatus.INTERNAL_SERVER_ERROR)
              .message(ERROR_DURING_LICENSE_VALIDATION)
              .build();
    }
    return validationResult;
  }

  public LicenseValidationResult getUserLicenseDetailsByToken(LicenseValidationRequest request) {

    LicenseValidationResult validationResult = null;
    try {
      tokenValidationService.validateTokenRequest(request);

      validationResult =
          new LicenseValidationResult.Builder().valid(true).message(TOKEN_IS_VALID).build();

    } catch (TokenExpiredException e) {
      validationResult = getTokenLicenseValidationResult(request, e.getEncUserId());
    } catch (LicenseServiceExceptionImpl e) {
      logger.error(LICENSE_VALIDATION_FAILED, e);
      validationResult =
          new LicenseValidationResult.Builder()
              .valid(false)
              .serviceStatus(e.getStatus())
              .message(e.getMessage())
              .build();
    } catch (Exception e) {
      logger.error(ERROR_DURING_LICENSE_VALIDATION, e);
      validationResult =
          new LicenseValidationResult.Builder()
              .valid(false)
              .serviceStatus(LicenseServiceStatus.UNKNOWN_ERROR)
              .message(ERROR_DURING_LICENSE_VALIDATION)
              .build();
    }
    return validationResult;
  }

  private LicenseValidationResult getTokenLicenseValidationResult(
      LicenseValidationRequest request, String encUserId) {
    LicenseValidationResult validationResult = null;
    try {
      String userId = userIdEncryptor.decrypt(encUserId);
      LicenseInfo licenseInfo = licenseDetailsService.getAndValidateLicenseDetails(request, userId);
      validationResult =
          getValidationResult(
              request.instanceId(),
              licenseInfo,
              LicenseServiceStatus.TOKEN_REFRESHED,
              TOKEN_REFRESHED);

    } catch (LicenseServiceExceptionImpl e) {
      logger.error(LICENSE_VALIDATION_FAILED, e);
      validationResult =
          new LicenseValidationResult.Builder()
              .valid(false)
              .serviceStatus(e.getStatus())
              .message(e.getMessage())
              .build();

    } catch (Exception ge) {
      logger.error(ERROR_DURING_LICENSE_VALIDATION, ge);
      validationResult =
          new LicenseValidationResult.Builder()
              .valid(false)
              .serviceStatus(LicenseServiceStatus.UNKNOWN_ERROR)
              .message(ERROR_DURING_LICENSE_VALIDATION)
              .build();
    }
    return validationResult;
  }

  private LicenseValidationResult getValidationResult(
      String instanceId, LicenseInfo info, LicenseServiceStatus errorCode, String message) {
    String obfUserID = userIdEncryptor.encrypt(info.userId());
    String licenseTier = info.licenseTier();

    return new LicenseValidationResult.Builder()
        .userId(obfUserID)
        .appInstanceId(instanceId)
        .valid(true)
        .licenseStatus(info.licenseStatus())
        .licenseTier(licenseTier)
        .serviceStatus(errorCode)
        .message(message)
        .build();
  }
}
