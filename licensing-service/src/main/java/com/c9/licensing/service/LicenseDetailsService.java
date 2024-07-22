package com.c9.licensing.service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseValidationRequest;

public interface LicenseDetailsService {
	
	String TOKEN_ALREADY_EXIST = "Token already exists. Use the existing token for license check.";
	
	LicenseInfo validateAndGetLicenseDetailsByLicenseKey(LicenseValidationRequest request) throws Exception;
	
	LicenseInfo validateAndGetLicenseDetailsByUserId(String encUserId, LicenseValidationRequest request) throws Exception;

}
