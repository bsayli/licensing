package io.github.bsayli.licensing.service.user;

import io.github.bsayli.licensing.model.LicenseInfo;
import java.util.Optional;

public interface UserOrchestrationService {

  void updateLicenseUsage(String userId, String appInstanceId);

  Optional<LicenseInfo> getUser(String userId);
}
