package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.LicenseValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LicenseService {

  String ERROR_DURING_LICENSE_VALIDATION = "An unexpected error occurred during license validation";
  String LICENSE_VALIDATION_FAILED = "License validation failed";
  String LICENSE_KEY_IS_VALID = "License key is valid";
  String TOKEN_IS_VALID = "Token is valid";
  String TOKEN_REFRESHED = "Token is refreshed";
  Logger logger = LoggerFactory.getLogger(LicenseService.class);

  LicenseValidationResult getUserLicenseDetailsByLicenseKey(LicenseValidationRequest request);

  LicenseValidationResult getUserLicenseDetailsByToken(LicenseValidationRequest request);
}
