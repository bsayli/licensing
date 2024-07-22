package com.c9.licensing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LicenseTokenValidationService {

	String TOKEN_HAS_EXPIRED = "Token has expired!";
	String ERROR_DURING_TOKEN_VALIDATION = "An unexpected error occurred during token validation";
	String TOKEN_INVALID = "Token is invalid!";
	String TOKEN_INVALID_ACCESS = "Token invalid access!";
	String TOKEN_IS_TOO_OLD_FOR_REFRESH = "The provided token is no longer valid for refresh due to exceeding its refresh window. Please request a new token";
	Logger logger = LoggerFactory.getLogger(LicenseTokenValidationService.class);

	void validateToken(String token, String requestedInstanceId);
}
