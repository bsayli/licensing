package io.github.bsayli.licensing.service.jwt.impl;

import io.github.bsayli.licensing.cache.CacheNames;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import java.util.Objects;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

  private static final Boolean MARKER = Boolean.TRUE;

  private final ClientSessionCacheService sessionCache;
  private final Cache blacklist;

  public JwtBlacklistServiceImpl(
      ClientSessionCacheService sessionCache, CacheManager cacheManager) {
    this.sessionCache = Objects.requireNonNull(sessionCache, "sessionCache");
    this.blacklist = requireCache(cacheManager, CacheNames.BLACKLISTED_TOKENS);
  }

  private static Cache requireCache(CacheManager mgr, String name) {
    Cache c = mgr.getCache(name);
    if (c == null) throw new IllegalStateException("Cache not configured: " + name);
    return c;
  }

  @Override
  public void addCurrentTokenToBlacklist(String clientId) {
    Optional<ClientSessionSnapshot> cached = sessionCache.find(clientId);
    cached.map(ClientSessionSnapshot::licenseToken).ifPresent(this::put);
  }

  @Override
  public boolean isBlacklisted(String token) {
    if (token == null) return false;
    return Boolean.TRUE.equals(blacklist.get(token, Boolean.class));
  }

  private void put(String token) {
    if (token != null && !token.isBlank()) {
      blacklist.put(token, MARKER);
    }
  }
}
