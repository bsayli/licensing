package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import java.util.Optional;

public interface ClientSessionCacheService {

  void put(ClientInfo clientInfo);

  Optional<ClientSessionSnapshot> find(String clientId);
}
