package io.github.bsayli.licensing.service.jwt.impl;

import io.github.bsayli.licensing.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.service.LicenseClientCacheManagementService;
import io.github.bsayli.licensing.service.jwt.JwtBlacklistService;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

  private static final String CACHE_NAME_BLACKLIST_TOKENS = "blacklistedTokens";

  private final LicenseClientCacheManagementService clientCacheManagementService;
  private final CacheManager cacheManager;

  public JwtBlacklistServiceImpl(
      LicenseClientCacheManagementService clientCacheManagementService, CacheManager cacheManager) {
    this.clientCacheManagementService = clientCacheManagementService;
    this.cacheManager = cacheManager;
  }

  @Override
  public void addCurrentTokenToBlacklist(String clientId) {
    Optional<ClientCachedLicenseData> cachedData =
        clientCacheManagementService.getClientCachedLicenseData(clientId);
    if (cachedData.isPresent()) {
      String licenseToken = cachedData.get().getLicenseToken();
      addToCache(licenseToken);
    }
  }

  @Override
  public boolean isBlackListed(String licenseToken) {
    boolean isBlackListed = false;
    Cache blacklistedTokenCache = cacheManager.getCache(CACHE_NAME_BLACKLIST_TOKENS);
    if (blacklistedTokenCache != null) {
      ValueWrapper valueWrapper = blacklistedTokenCache.get(licenseToken);
      if (valueWrapper != null && valueWrapper.get() != null) {
        isBlackListed = true;
      }
    }
    return isBlackListed;
  }

  private void addToCache(String licenseToken) {
    Cache blacklistedTokenCache = cacheManager.getCache(CACHE_NAME_BLACKLIST_TOKENS);
    if (blacklistedTokenCache != null) {
      blacklistedTokenCache.put(licenseToken, Optional.empty());
    }
  }
}
