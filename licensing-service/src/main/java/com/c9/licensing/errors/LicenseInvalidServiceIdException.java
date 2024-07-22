package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseInvalidServiceIdException extends RuntimeException implements LicenseServiceException{
	
	private static final long serialVersionUID = 7668179223994149753L;

	public LicenseInvalidServiceIdException(String message) {
		super(message);
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.LICENSE_INVALID_SERVICE_ID;
	}
}