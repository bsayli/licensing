package io.github.bsayli.licensing.sdk.service;

import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseToken;

public interface LicenseOrchestrationService {

  LicenseToken getLicenseToken(LicenseAccessRequest request);
}
