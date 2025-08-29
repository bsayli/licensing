package com.c9.licensing.service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseValidationRequest;

public interface LicenseDetailsService {

  LicenseInfo getAndValidateLicenseDetails(LicenseValidationRequest request, String userId)
      throws Exception;
}
