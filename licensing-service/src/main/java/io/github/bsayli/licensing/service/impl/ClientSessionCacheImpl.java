package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.domain.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.ClientSessionCache;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class ClientSessionCacheImpl implements ClientSessionCache {

  private static final String CACHE_ACTIVE_CLIENTS = "activeClients";

  private final CacheManager cacheManager;
  private final ClientIdGenerator clientIdGenerator;

  public ClientSessionCacheImpl(CacheManager cacheManager, ClientIdGenerator clientIdGenerator) {
    this.cacheManager = cacheManager;
    this.clientIdGenerator = clientIdGenerator;
  }

  @Override
  public void put(ClientInfo info) {
    Cache cache = cacheManager.getCache(CACHE_ACTIVE_CLIENTS);
    if (cache == null) return;

    String clientId = clientIdGenerator.getClientId(info);
    ClientCachedLicenseData snapshot =
        new ClientCachedLicenseData.Builder()
            .licenseToken(info.licenseToken())
            .encUserId(info.encUserId())
            .serviceId(info.serviceId())
            .serviceVersion(info.serviceVersion())
            .checksum(info.checksum())
            .build();

    cache.put(clientId, snapshot);
  }

  @Override
  public Optional<ClientCachedLicenseData> find(String clientId) {
    Cache cache = cacheManager.getCache(CACHE_ACTIVE_CLIENTS);
    if (cache == null) return Optional.empty();

    Cache.ValueWrapper w = cache.get(clientId);
    return (w == null || w.get() == null)
        ? Optional.empty()
        : Optional.of((ClientCachedLicenseData) w.get());
  }
}
