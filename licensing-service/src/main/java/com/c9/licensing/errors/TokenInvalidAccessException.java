package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

import io.jsonwebtoken.JwtException;

public class TokenInvalidAccessException extends JwtException implements LicenseException{

	private static final long serialVersionUID = -2458534544543529495L;

	public TokenInvalidAccessException(String message) {
		super(message);
	}
	
	public TokenInvalidAccessException(String message, Throwable e) {
		super(message, e);
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.TOKEN_INVALID_ACCESS;
	}
}