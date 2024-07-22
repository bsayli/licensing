package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

import io.jsonwebtoken.JwtException;

public class TokenInvalidException extends JwtException implements LicenseServiceException{

	private static final long serialVersionUID = 4398609211361201185L;

	public TokenInvalidException(String message) {
		super(message);
	}
	
	public TokenInvalidException(String message, Throwable e) {
		super(message, e);
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.TOKEN_INVALID;
	}
}