package com.c9.licensing.sdk.service;

import com.c9.licensing.sdk.model.LicenseValidationRequest;
import com.c9.licensing.sdk.model.LicenseValidationResponse;

public interface LicenseOrchestrationService {

	LicenseValidationResponse getLicenseDetails(LicenseValidationRequest request);
}
