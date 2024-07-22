package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class TokenForbiddenAccessException extends RuntimeException implements LicenseServiceException{

	private static final long serialVersionUID = -2458534544543529495L;

	public TokenForbiddenAccessException(String message) {
		super(message);
	}
	
	public TokenForbiddenAccessException(String message, Throwable e) {
		super(message, e);
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.TOKEN_INVALID_ACCESS;
	}
}