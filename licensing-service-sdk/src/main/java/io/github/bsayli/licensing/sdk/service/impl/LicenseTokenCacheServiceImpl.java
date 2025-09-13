package io.github.bsayli.licensing.sdk.service.impl;

import static io.github.bsayli.licensing.sdk.cache.CacheNames.CACHE_LICENSE_TOKENS;

import io.github.bsayli.licensing.sdk.service.LicenseTokenCacheService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

@Service
public class LicenseTokenCacheServiceImpl implements LicenseTokenCacheService {

  private final Cache cache;

  public LicenseTokenCacheServiceImpl(@Qualifier(CACHE_LICENSE_TOKENS) Cache cache) {
    this.cache = cache;
  }

  @Override
  public void put(String clientId, String licenseToken) {
    cache.put(clientId, licenseToken);
  }

  @Override
  public String get(String clientId) {
    return cache.get(clientId, String.class);
  }

  @Override
  public void evict(String clientId) {
    cache.evict(clientId);
  }
}
