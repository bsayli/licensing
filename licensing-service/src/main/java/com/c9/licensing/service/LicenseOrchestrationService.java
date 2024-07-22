package com.c9.licensing.service;

import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.response.LicenseValidationResponse;

public interface LicenseOrchestrationService {

	LicenseValidationResponse getLicenseDetails(LicenseValidationRequest request);
	
	LicenseValidationResponse getLicenseDetailsByLicenseKey(LicenseValidationRequest request);
	
	LicenseValidationResponse getLicenseDetailsByToken(LicenseValidationRequest request);
}
