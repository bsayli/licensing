package io.github.bsayli.licensing.service.user.orchestration;

import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface UserCacheManagementService {

  void refreshAsync(String userId);

  LicenseInfo getOffline(String userId);

  boolean isOnlineMissing(String userId);

  void putOffline(String userId, LicenseInfo licenseInfo);

  void putBoth(String userId, LicenseInfo licenseInfo);

  void evict(String userId);
}
