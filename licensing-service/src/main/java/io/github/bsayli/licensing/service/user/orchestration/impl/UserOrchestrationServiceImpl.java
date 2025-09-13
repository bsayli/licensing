package io.github.bsayli.licensing.service.user.orchestration.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.user.core.UserService;
import io.github.bsayli.licensing.service.user.orchestration.UserCacheManagementService;
import io.github.bsayli.licensing.service.user.orchestration.UserOrchestrationService;
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
  public LicenseInfo getLicenseInfo(String userId) {
    LicenseInfo offline = userCacheManagementService.getOffline(userId);
    if (offline != null) {
      if (userCacheManagementService.isOnlineMissing(userId)) {
        userCacheManagementService.refreshAsync(userId);
      }
      return offline;
    }

    LicenseInfo online = userService.getUser(userId);
    userCacheManagementService.putOffline(userId, online);
    return online;
  }

  @Override
  public void recordUsage(String userId, String instanceId) {
    LicenseInfo updated = userService.updateLicenseUsage(userId, instanceId);
    userCacheManagementService.putBoth(userId, updated);
  }
}
