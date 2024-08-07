package com.c9.licensing.service;

import java.util.Optional;

import com.c9.licensing.model.ClientCachedLicenseData;
import com.c9.licensing.model.ClientInfo;

public interface LicenseClientCacheManagementService {

	void addClientInfo(ClientInfo clientInfo);

	Optional<ClientCachedLicenseData> getClientCachedLicenseData(String clientId);

}
