package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseErrorCode;

public class LicenseExpiredException extends RuntimeException implements LicenseException{

	private static final long serialVersionUID = -4216944794327386510L;

	public LicenseExpiredException(String message) {
		super(message);
	}

	public LicenseErrorCode getErrorCode() {
		return LicenseErrorCode.LICENSE_EXPIRED;
	}
}