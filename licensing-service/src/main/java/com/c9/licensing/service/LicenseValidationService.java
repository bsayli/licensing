package com.c9.licensing.service;

import com.c9.licensing.model.LicenseInfo;

public interface LicenseValidationService {

	String LICENSE_LIMIT_EXCEEDED = "License usage limit exceeded. You can only activate this license on %d machines. " +
	"Please deactivate it on another machine or upgrade your license.";

	String LICENSE_NOT_ACTIVE = "Your license is currently inactive. Please contact support for assistance.";

	String LICENSE_EXPIRED = "Your license has expired. Please renew it to continue using the application.";

	
	LicenseInfo validateLicense(String encLicenseKey, String instanceId) throws Exception;
	
	LicenseInfo validateLicenseForToken(String tokenSub, String instanceId) throws Exception;

}
