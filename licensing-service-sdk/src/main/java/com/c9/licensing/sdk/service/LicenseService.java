package com.c9.licensing.sdk.service;

import com.c9.licensing.sdk.model.server.LicenseServerValidationRequest;
import com.c9.licensing.sdk.model.server.LicenseServerValidationResponse;

public interface LicenseService {
	
	String URI_LICENSE_VALIDATE = "/api/license/v2/validate";
	String REQUEST_PARAM_SERVICE_ID = "serviceId";
	String REQUEST_PARAM_SERVICE_VERSION = "serviceVersion";
	String REQUEST_PARAM_LICENSE_KEY = "licenseKey";
	String HEADER_PARAM_LICENSE_TOKEN = "X-License-Token";
	String HEADER_PARAM_INSTANCE_ID = "X-Instance-ID";
	String HEADER_PARAM_CHECKSUM = "X-Checksum";
	String HEADER_PARAM_SIGNATURE = "X-Signature";
	String HEADER_PARAM_FORCE_TOKEN_REFRESH = "X-Force-Token-Refresh";
	
	LicenseServerValidationResponse getLicenseDetails(LicenseServerValidationRequest request);

}
