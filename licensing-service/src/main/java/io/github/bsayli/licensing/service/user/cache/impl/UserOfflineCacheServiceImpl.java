package io.github.bsayli.licensing.service.user.cache.impl;

import static io.github.bsayli.licensing.cache.CacheNames.CACHE_USER_OFFLINE_INFO;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.user.cache.UserCacheService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

@Service("userOfflineCacheService")
public class UserOfflineCacheServiceImpl implements UserCacheService {

  private final Cache cache;

  public UserOfflineCacheServiceImpl(@Qualifier(CACHE_USER_OFFLINE_INFO) Cache cache) {
    this.cache = cache;
  }

  @Override
  public LicenseInfo get(String userId) {
    return cache.get(userId, LicenseInfo.class);
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
