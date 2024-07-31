package com.c9.licensing.service.validation;

import java.util.List;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseValidationRequest;

public interface LicenseValidationService {
	
	String MESSAGE_LICENSE_LIMIT_EXCEEDED = "License usage limit exceeded. You can only activate this license on %d machines. " +
	"Please deactivate it on another machine or upgrade your license.";

	String MESSAGE_LICENSE_NOT_ACTIVE = "Your license is currently inactive. Please contact support for assistance.";

	String MESSAGE_LICENSE_EXPIRED = "Your license has expired. Please renew it to continue using the application.";
	
	String MESSAGE_LICENSE_SERVICE_ID_NOT_SUPPORTED = "Your license does not support this service %s. Please contact support for assistance.";
	
	String MESSAGE_LICENSE_INVALID_SERVICE_ID = "The requested service id  %s is invalid. Please verify the service id and try again.";
	
	String MESSAGE_LICENSE_INVALID_CHECKSUM = "Checksum validation failed. Please contact support for assistance.";
	
	String MESSAGE_LICENSE_SERVICE_VERSION_NOT_SUPPORTED = "Your license does not support this %s version. Please contact support for assistance.";

	void validate(LicenseInfo licenseInfo, LicenseValidationRequest request);

	boolean isInstanceIdNotExist(String instanceId, List<String> instanceIds);

}
