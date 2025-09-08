package io.github.bsayli.licensing.service.user.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.user.UserCacheManagementService;
import io.github.bsayli.licensing.service.user.cache.UserCacheService;
import io.github.bsayli.licensing.service.user.operations.UserAsyncService;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserCacheManagementServiceImpl implements UserCacheManagementService {

  private static final Logger log = LoggerFactory.getLogger(UserCacheManagementServiceImpl.class);

  private final UserCacheService offlineCache;
  private final UserCacheService onlineCache;
  private final UserAsyncService userAsyncService;

  public UserCacheManagementServiceImpl(
      UserCacheService userOfflineCacheService,
      UserCacheService userOnlineCacheService,
      UserAsyncService userAsyncService) {
    this.offlineCache = userOfflineCacheService;
    this.onlineCache = userOnlineCacheService;
    this.userAsyncService = userAsyncService;
  }

  @Override
  public void refreshAsync(String userId) {
    CompletableFuture<Optional<LicenseInfo>> future = userAsyncService.getUser(userId);
    future.whenComplete(
        (opt, ex) -> {
          if (ex != null) {
            log.warn(
                "Async refresh failed for userId={} ({}: {})",
                userId,
                ex.getClass().getSimpleName(),
                ex.getMessage());
            return;
          }
          if (opt.isPresent()) {
            LicenseInfo info = opt.get();
            onlineCache.put(userId, info);
            offlineCache.put(userId, info);
          } else {
            onlineCache.evict(userId);
            offlineCache.evict(userId);
          }
          log.debug("Async refresh completed for userId={}", userId);
        });
  }

  @Override
  public Optional<LicenseInfo> getOffline(String userId) {
    return offlineCache.get(userId);
  }

  @Override
  public boolean isOnlineMissing(String userId) {
    return !onlineCache.exists(userId);
  }

  @Override
  public void putOffline(String userId, LicenseInfo licenseInfo) {
    if (licenseInfo == null) {
      offlineCache.evict(userId);
    } else {
      offlineCache.put(userId, licenseInfo);
    }
  }

  @Override
  public void putBoth(String userId, LicenseInfo licenseInfo) {
    if (licenseInfo == null) {
      onlineCache.evict(userId);
      offlineCache.evict(userId);
    } else {
      onlineCache.put(userId, licenseInfo);
      offlineCache.put(userId, licenseInfo);
    }
  }

  @Override
  public void evict(String userId) {
    onlineCache.evict(userId);
    offlineCache.evict(userId);
  }
}
