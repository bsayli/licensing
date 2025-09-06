package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.model.ClientInfo;
import java.util.Optional;

public interface LicenseClientCacheManagementService {

  void addClientInfo(ClientInfo clientInfo);

  Optional<ClientCachedLicenseData> getClientCachedLicenseData(String clientId);
}
