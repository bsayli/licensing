package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

public class LicenseUsageLimitExceededException extends RuntimeException implements LicenseException{

	private static final long serialVersionUID = -7483968084676456879L;


	public LicenseUsageLimitExceededException(String message) {
		super(message);
		
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.LICENSE_INVALID;
	}
}