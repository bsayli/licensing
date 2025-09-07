package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.model.LicenseValidationResult;

public interface LicenseValidationService {

  LicenseValidationResult validateLicense(IssueTokenRequest request);

  LicenseValidationResult validateLicense(ValidateTokenRequest request, String token);
}
