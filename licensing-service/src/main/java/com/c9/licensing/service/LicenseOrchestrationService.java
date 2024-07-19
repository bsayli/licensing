package com.c9.licensing.service;

import com.c9.licensing.response.LicenseValidationResponse;

public interface LicenseOrchestrationService {

	
	LicenseValidationResponse getLicenseDetails(String licenseKey, String instanceId);
	
	LicenseValidationResponse getLicenseDetailsByToken(String token, String instanceId);
}
