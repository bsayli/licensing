package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;

public interface ClientSessionCacheService {

  void put(ClientInfo clientInfo);

  ClientSessionSnapshot find(String clientId);
}
