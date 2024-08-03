package com.c9.licensing.service.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c9.licensing.model.LicenseValidationRequest;

public interface LicenseTokenRequestValidationService {

	String MESSAGE_INVALID_REQUEST = "Invalid request, client request parameters were changed unexpectedly!";
	String MESSAGE_TOKEN_HAS_EXPIRED = "Token has expired!";
	String MESSAGE_ERROR_DURING_TOKEN_VALIDATION = "An unexpected error occurred during token validation";
	String MESSAGE_TOKEN_INVALID = "Token is invalid!";
	String MESSAGE_TOKEN_INVALIDATED_BY_FORCE_REFRESH = "License token was invalidated by force refresh!";
	String MESSAGE_TOKEN_INVALID_ACCESS = "Token invalid access, client request parameters were changed!";
	String MESSAGE_TOKEN_IS_TOO_OLD_FOR_REFRESH = "The provided token is no longer valid for refresh due to exceeding its refresh window. Please request a new token";
	Logger logger = LoggerFactory.getLogger(LicenseTokenRequestValidationService.class);

	void validateToken(LicenseValidationRequest request);
}
