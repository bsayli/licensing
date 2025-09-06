package io.github.bsayli.licensing.sdk.service;

import io.github.bsayli.licensing.sdk.model.LicenseValidationRequest;
import io.github.bsayli.licensing.sdk.model.LicenseValidationResponse;

public interface LicenseOrchestrationService {

  LicenseValidationResponse getLicenseDetails(LicenseValidationRequest request);
}
