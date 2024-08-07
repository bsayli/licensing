package com.c9.licensing.model.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class TokenExpiredException extends LicenseServiceExceptionImpl{

	private static final long serialVersionUID = 2818307805521893164L;
	
	private final String encUserId;

	public TokenExpiredException(String encUserId, String message) {
		super(message);
		this.encUserId = encUserId;
	}

	public String getEncUserId() {
		return encUserId;
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.TOKEN_EXPIRED;
	}
}