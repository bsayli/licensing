package io.github.bsayli.licensing.service.jwt.impl;

import io.github.bsayli.licensing.domain.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.service.ClientSessionCache;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import java.util.Objects;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

  private static final String BLACKLIST_CACHE = "blacklistedTokens";
  private static final Boolean MARKER = Boolean.TRUE;

  private final ClientSessionCache sessionCache;
  private final Cache blacklist;

  public JwtBlacklistServiceImpl(ClientSessionCache sessionCache, CacheManager cacheManager) {
    this.sessionCache = Objects.requireNonNull(sessionCache, "sessionCache");
    this.blacklist =
        Objects.requireNonNull(
            cacheManager.getCache(BLACKLIST_CACHE), "Cache not configured: " + BLACKLIST_CACHE);
  }

  @Override
  public void addCurrentTokenToBlacklist(String clientId) {
    Optional<ClientCachedLicenseData> cached = sessionCache.find(clientId);
    cached.map(ClientCachedLicenseData::getLicenseToken).ifPresent(this::put);
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
