package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

import io.jsonwebtoken.JwtException;

public class TokenExpiredException extends JwtException implements LicenseException{

	private static final long serialVersionUID = 2818307805521893164L;
	
	private final String tokenSub;

	public TokenExpiredException(String tokenSub, String message) {
		super(message);
		this.tokenSub = tokenSub;
	}

	public String getTokenSub() {
		return tokenSub;
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.TOKEN_EXPIRED;
	}
}