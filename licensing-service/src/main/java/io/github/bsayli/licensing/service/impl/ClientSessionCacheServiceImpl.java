package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.cache.CacheNames;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import java.util.Objects;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class ClientSessionCacheServiceImpl implements ClientSessionCacheService {

  private final Cache cache;
  private final ClientIdGenerator clientIdGenerator;

  public ClientSessionCacheServiceImpl(
      CacheManager cacheManager, ClientIdGenerator clientIdGenerator) {
    this.cache = requireCache(cacheManager, CacheNames.ACTIVE_CLIENTS);
    this.clientIdGenerator = clientIdGenerator;
  }

  private static Cache requireCache(CacheManager mgr, String name) {
    Cache c = mgr.getCache(name);
    if (c == null) throw new IllegalStateException("Cache not configured: " + name);
    return c;
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
  public Optional<ClientSessionSnapshot> find(String clientId) {
    Cache.ValueWrapper w = cache.get(clientId);
    return (w == null || w.get() == null)
        ? Optional.empty()
        : Optional.of((ClientSessionSnapshot) Objects.requireNonNull(w.get()));
  }
}
