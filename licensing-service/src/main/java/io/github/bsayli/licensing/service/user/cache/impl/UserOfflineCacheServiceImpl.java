package io.github.bsayli.licensing.service.user.cache.impl;

import io.github.bsayli.licensing.cache.CacheNames;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.user.cache.UserCacheService;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service("userOfflineCacheService")
public class UserOfflineCacheServiceImpl implements UserCacheService {

  private final Cache cache;

  public UserOfflineCacheServiceImpl(CacheManager cacheManager) {
    this.cache = requireCache(cacheManager, CacheNames.USER_OFFLINE_INFO);
  }

  private static Cache requireCache(CacheManager mgr, String name) {
    Cache c = mgr.getCache(name);
    if (c == null) throw new IllegalStateException("Cache not configured: " + name);
    return c;
  }

  @Override
  public Optional<LicenseInfo> get(String userId) {
    return Optional.ofNullable(cache.get(userId, LicenseInfo.class));
  }

  @Override
  public void put(String userId, LicenseInfo licenseInfo) {
    cache.put(userId, licenseInfo);
  }

  @Override
  public void evict(String userId) {
    cache.evict(userId);
  }

  @Override
  public boolean exists(String userId) {
    return cache.get(userId) != null;
  }
}
