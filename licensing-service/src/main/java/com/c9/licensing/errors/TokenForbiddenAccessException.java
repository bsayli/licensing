package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

public class TokenForbiddenAccessException extends RuntimeException implements LicenseException{

	private static final long serialVersionUID = -2458534544543529495L;

	public TokenForbiddenAccessException(String message) {
		super(message);
	}
	
	public TokenForbiddenAccessException(String message, Throwable e) {
		super(message, e);
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.TOKEN_INVALID_ACCESS;
	}
}