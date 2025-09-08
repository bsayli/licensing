package io.github.bsayli.licensing.service.user.cache.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.user.cache.UserCacheService;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service("userOfflineCacheService")
public class UserOfflineCacheServiceImpl implements UserCacheService {

  private static final String CACHE_NAME = "userOfflineInfoCache";

  private final CacheManager cacheManager;

  public UserOfflineCacheServiceImpl(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Override
  public Optional<LicenseInfo> get(String userId) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache == null) return Optional.empty();
    LicenseInfo info = cache.get(userId, LicenseInfo.class);
    return Optional.ofNullable(info);
  }

  @Override
  public void put(String userId, LicenseInfo licenseInfo) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.put(userId, licenseInfo);
    }
  }

  @Override
  public void evict(String userId) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.evict(userId);
    }
  }

  @Override
  public boolean exists(String userId) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache == null) return false;
    return cache.get(userId) != null;
  }
}
