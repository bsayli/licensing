package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseServiceVersionNotSupportedException extends RuntimeException implements LicenseServiceException{

	private static final long serialVersionUID = -6851648046615340071L;

	public LicenseServiceVersionNotSupportedException(String message) {
		super(message);
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.LICENSE_SERVICE_VERSION_NOT_SUPPORTED;
	}
}