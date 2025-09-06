package io.github.bsayli.licensing.service.user;

import io.github.bsayli.licensing.model.LicenseInfo;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UserCacheManagementService {

  Logger logger = LoggerFactory.getLogger(UserCacheManagementService.class);

  void updateCachesAsync(String userId) throws Exception;

  Map<String, Optional<LicenseInfo>> getDataInOffline(String userId);

  boolean isOnlineCacheDataExpired(String userId);

  void refreshDataInOffline(String userId, Optional<LicenseInfo> licenseInfo);

  void refreshDataInCaches(String userId, Optional<LicenseInfo> licenseInfo);

  void evictDataInCaches(String userId);
}
