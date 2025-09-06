package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.LicenseInfo;

public interface LicenseDetailsService {

  LicenseInfo getAndValidateLicenseDetails(LicenseValidationRequest request, String userId)
      throws Exception;
}
