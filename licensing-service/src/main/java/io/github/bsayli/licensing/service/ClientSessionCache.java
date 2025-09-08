package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.domain.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import java.util.Optional;

public interface ClientSessionCache {

  void put(ClientInfo clientInfo);

  Optional<ClientCachedLicenseData> find(String clientId);
}
