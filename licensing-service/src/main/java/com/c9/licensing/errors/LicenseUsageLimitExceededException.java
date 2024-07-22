package com.c9.licensing.errors;

import com.c9.licensing.model.LicenseServiceStatus;

public class LicenseUsageLimitExceededException extends RuntimeException implements LicenseServiceException{

	private static final long serialVersionUID = -7483968084676456879L;


	public LicenseUsageLimitExceededException(String message) {
		super(message);
		
	}

	public LicenseServiceStatus getStatus() {
		return LicenseServiceStatus.LICENSE_USAGE_LIMIT_EXCEEDED;
	}
}