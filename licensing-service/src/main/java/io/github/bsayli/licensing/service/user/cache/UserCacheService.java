package io.github.bsayli.licensing.service.user.cache;

import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface UserCacheService {

  LicenseInfo get(String userId);

  void put(String userId, LicenseInfo licenseInfo);

  void evict(String userId);

  boolean exists(String userId);
}
