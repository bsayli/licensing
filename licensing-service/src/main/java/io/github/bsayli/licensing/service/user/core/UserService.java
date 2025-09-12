package io.github.bsayli.licensing.service.user.core;

import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface UserService {
  LicenseInfo getUser(String userId);

  LicenseInfo updateLicenseUsage(String userId, String instanceId);
}
