package io.github.bsayli.licensing.service.user.orchestration.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.user.core.UserService;
import io.github.bsayli.licensing.service.user.orchestration.UserCacheManagementService;
import io.github.bsayli.licensing.service.user.orchestration.UserOrchestrationService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserOrchestrationServiceImpl implements UserOrchestrationService {

  private final UserService userService;
  private final UserCacheManagementService userCacheManagementService;

  public UserOrchestrationServiceImpl(
      UserService userService, UserCacheManagementService userCacheManagementService) {
    this.userService = userService;
    this.userCacheManagementService = userCacheManagementService;
  }

  @Override
  public Optional<LicenseInfo> getLicenseInfo(String userId) {
    Optional<LicenseInfo> offline = userCacheManagementService.getOffline(userId);
    if (offline.isPresent()) {
      if (userCacheManagementService.isOnlineMissing(userId)) {
        userCacheManagementService.refreshAsync(userId);
      }
      return offline;
    }

    Optional<LicenseInfo> online = userService.getUser(userId);
    userCacheManagementService.putOffline(userId, online.orElse(null));
    return online;
  }

  @Override
  public void recordUsage(String userId, String instanceId) {
    Optional<LicenseInfo> updated = userService.updateLicenseUsage(userId, instanceId);
    userCacheManagementService.putBoth(userId, updated.orElse(null));
  }
}
