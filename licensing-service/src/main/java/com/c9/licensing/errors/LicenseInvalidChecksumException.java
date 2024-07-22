package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseInvalidChecksumException extends RuntimeException implements LicenseServiceException{
	private static final long serialVersionUID = 864714349412856278L;

	public LicenseInvalidChecksumException(String message) {
		super(message);
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.LICENSE_SERVICE_INVALID_CHECKSUM;
	}
}