package io.github.bsayli.licensing.repository.user;

import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface UserRepository {
  LicenseInfo getUser(String userId);

  LicenseInfo updateLicenseUsage(String userId, String appInstanceId);
}
