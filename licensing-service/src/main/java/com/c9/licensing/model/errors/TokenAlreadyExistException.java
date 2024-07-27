package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class TokenAlreadyExistException extends LicenseServiceExceptionImpl {
	
	private static final long serialVersionUID = 9039245175884123100L;
	
	private final String token;

	public TokenAlreadyExistException(String token, String message) {
		super(message);
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.TOKEN_ALREADY_EXIST;
	}
}