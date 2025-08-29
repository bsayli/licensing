package com.c9.licensing.service;

import com.c9.licensing.model.ClientCachedLicenseData;
import com.c9.licensing.model.ClientInfo;
import java.util.Optional;

public interface LicenseClientCacheManagementService {

  void addClientInfo(ClientInfo clientInfo);

  Optional<ClientCachedLicenseData> getClientCachedLicenseData(String clientId);
}
