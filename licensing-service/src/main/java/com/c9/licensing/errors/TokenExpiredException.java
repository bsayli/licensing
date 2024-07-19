package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

import io.jsonwebtoken.JwtException;

public class TokenExpiredException extends JwtException implements LicenseException{

	private static final long serialVersionUID = 2818307805521893164L;
	
	private final String encUserId;

	public TokenExpiredException(String encUserId, String message) {
		super(message);
		this.encUserId = encUserId;
	}

	public String getEncUserId() {
		return encUserId;
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.TOKEN_EXPIRED;
	}
}