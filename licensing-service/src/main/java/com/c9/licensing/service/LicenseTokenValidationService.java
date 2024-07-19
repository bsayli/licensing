package com.c9.licensing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LicenseTokenValidationService {

	String TOKEN_HAS_EXPIRED = "Token has expired!";
	String ERROR_DURING_TOKEN_VALIDATION = "An unexpected error occurred during token validation";
	String TOKEN_INVALID = "Token invalid!";
	String TOKEN_INVALID_ACCESS = "Token invalid access!";
	Logger logger = LoggerFactory.getLogger(LicenseTokenValidationService.class);
	
	void validateToken(String token, String requestedInstanceId);
}
