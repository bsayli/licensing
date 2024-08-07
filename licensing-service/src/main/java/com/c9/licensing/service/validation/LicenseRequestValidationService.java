package com.c9.licensing.service.validation;

import com.c9.licensing.model.LicenseValidationRequest;

public interface LicenseRequestValidationService {
	
	String MESSAGE_TOKEN_ALREADY_EXIST = "Token already exists. Use the existing token for license check.";
	String MESSAGE_INVALID_REQUEST = "Invalid request, client request parameters were changed!";
	
	void checkLicenseKeyRequestWithCachedData(LicenseValidationRequest request, String userId);

}
