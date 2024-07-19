package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

public class TokenAlreadyExistException  extends RuntimeException implements LicenseException{
	
	private static final long serialVersionUID = 9039245175884123100L;
	
	private final String token;

	public TokenAlreadyExistException(String token, String message) {
		super(message);
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.TOKEN_ALREADY_EXIST;
	}
}