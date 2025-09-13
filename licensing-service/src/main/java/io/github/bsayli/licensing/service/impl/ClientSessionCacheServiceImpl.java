package io.github.bsayli.licensing.service.impl;

import static io.github.bsayli.licensing.cache.CacheNames.CACHE_ACTIVE_CLIENTS;

import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

@Service
public class ClientSessionCacheServiceImpl implements ClientSessionCacheService {

  private final Cache cache;
  private final ClientIdGenerator clientIdGenerator;

  public ClientSessionCacheServiceImpl(
      @Qualifier(CACHE_ACTIVE_CLIENTS) Cache cache, ClientIdGenerator clientIdGenerator) {
    this.cache = cache;
    this.clientIdGenerator = clientIdGenerator;
  }

  @Override
  public void put(ClientInfo info) {
    String clientId = clientIdGenerator.getClientId(info);
    ClientSessionSnapshot snapshot =
        new ClientSessionSnapshot.Builder()
            .licenseToken(info.licenseToken())
            .encUserId(info.encUserId())
            .serviceId(info.serviceId())
            .serviceVersion(info.serviceVersion())
            .checksum(info.checksum())
            .build();
    cache.put(clientId, snapshot);
  }

  @Override
  public ClientSessionSnapshot find(String clientId) {
    return cache.get(clientId, ClientSessionSnapshot.class);
  }

  @Override
  public void evict(String clientId) {
    cache.evict(clientId);
  }
}
