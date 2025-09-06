package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;

public interface LicenseOrchestrationService {

  LicenseValidationResponse getLicenseDetails(LicenseValidationRequest request);

  LicenseValidationResponse getLicenseDetailsByLicenseKey(LicenseValidationRequest request);

  LicenseValidationResponse getLicenseDetailsByToken(LicenseValidationRequest request);
}
