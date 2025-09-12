package io.github.bsayli.licensing.service.jwt.impl;

import static io.github.bsayli.licensing.cache.CacheNames.CACHE_BLACKLISTED_TOKENS;

import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

  private static final Boolean MARKER = Boolean.TRUE;

  private final ClientSessionCacheService sessionCache;
  private final Cache blacklist;

  public JwtBlacklistServiceImpl(
      ClientSessionCacheService sessionCache,
      @Qualifier(CACHE_BLACKLISTED_TOKENS) Cache blacklist) {
    this.sessionCache = sessionCache;
    this.blacklist = blacklist;
  }

  @Override
  public void addCurrentTokenToBlacklist(String clientId) {
    ClientSessionSnapshot snapshot = sessionCache.find(clientId);
    if (snapshot != null) {
      put(snapshot.licenseToken());
    }
  }

  @Override
  public boolean isBlacklisted(String token) {
    if (token == null) {
      return false;
    }
    return Boolean.TRUE.equals(blacklist.get(token, Boolean.class));
  }

  private void put(String token) {
    if (token != null && !token.isBlank()) {
      blacklist.put(token, MARKER);
    }
  }
}
