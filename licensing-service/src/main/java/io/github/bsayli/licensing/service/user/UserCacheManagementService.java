package io.github.bsayli.licensing.service.user;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import java.util.Optional;

public interface UserCacheManagementService {

  void refreshAsync(String userId);

  Optional<LicenseInfo> getOffline(String userId);

  boolean isOnlineMissing(String userId);

  void putOffline(String userId, LicenseInfo licenseInfo);

  void putBoth(String userId, LicenseInfo licenseInfo);

  void evict(String userId);
}
