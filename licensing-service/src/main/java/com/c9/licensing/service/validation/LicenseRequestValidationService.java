package com.c9.licensing.service.validation;

import com.c9.licensing.model.LicenseValidationRequest;

public interface LicenseRequestValidationService {
	
	String TOKEN_ALREADY_EXIST = "Token already exists. Use the existing token for license check.";
	String INVALID_REQUEST = "Invalid request, client request parameters were changed!";
	
	void checkLicenseKeyRequestWithCachedData(LicenseValidationRequest request, String userId);

	void checkTokenRequestWithCachedData(LicenseValidationRequest request);

}
