package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;

public interface LicenseKeyRequestValidationService {

  String MESSAGE_TOKEN_ALREADY_EXIST =
      "Token already exists. Use the existing token for license check.";
  String MESSAGE_INVALID_REQUEST = "Invalid request, client request parameters were changed!";

  void checkLicenseKeyRequestWithCachedData(LicenseValidationRequest request, String userId);

  void checkSignature(LicenseValidationRequest request);
}
