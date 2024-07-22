package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseInactiveException extends RuntimeException implements LicenseServiceException{

    private static final long serialVersionUID = -7483968084676456879L;

	public LicenseInactiveException(String message) {
		super(message);
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.LICENSE_INACTIVE;
	}
}