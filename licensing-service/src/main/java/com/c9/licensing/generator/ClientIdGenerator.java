package com.c9.licensing.generator;

import com.c9.licensing.model.ClientInfo;
import com.c9.licensing.model.LicenseValidationRequest;

public interface ClientIdGenerator {
	
	String getClientId(LicenseValidationRequest request);
	
	String getClientId(ClientInfo clientInfo);

}
