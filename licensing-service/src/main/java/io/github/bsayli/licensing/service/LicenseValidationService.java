package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;

public interface LicenseValidationService {

  LicenseValidationResult validateLicense(IssueAccessRequest request);

  LicenseValidationResult validateLicense(ValidateAccessRequest request, String token);
}
