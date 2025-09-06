package io.github.bsayli.licensing.service.user.cache;

import io.github.bsayli.licensing.model.LicenseInfo;
import java.util.Map;
import java.util.Optional;

public interface UserCacheService {

  Optional<LicenseInfo> getUser(String userId);

  Optional<LicenseInfo> addUser(String userId, Optional<LicenseInfo> licenseInfo);

  void updateUser(String userId, Optional<LicenseInfo> licenseInfo);

  void evictUser(String userId);

  boolean userExistInCache(String userId);

  Map<String, Optional<LicenseInfo>> returnIfExist(String userId);
}
