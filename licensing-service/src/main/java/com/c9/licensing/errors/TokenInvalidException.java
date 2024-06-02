package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

import io.jsonwebtoken.JwtException;

public class TokenInvalidException extends JwtException implements LicenseException{

	private static final long serialVersionUID = 4398609211361201185L;

	public TokenInvalidException(String message) {
		super(message);
	}
	
	public TokenInvalidException(String message, Throwable e) {
		super(message, e);
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.TOKEN_INVALID;
	}
}