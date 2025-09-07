package io.github.bsayli.licensing.service.user.impl;

import io.github.bsayli.licensing.model.LicenseInfo;
import io.github.bsayli.licensing.model.errors.LicenseServiceUnexpectedException;
import io.github.bsayli.licensing.service.exception.ConnectionExceptionPredicate;
import io.github.bsayli.licensing.service.user.UserCacheManagementService;
import io.github.bsayli.licensing.service.user.operations.UserRecoverService;
import jakarta.ws.rs.ProcessingException;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserRecoverServiceImpl implements UserRecoverService {

  private final UserCacheManagementService userCacheManagementService;

  public UserRecoverServiceImpl(UserCacheManagementService userCacheManagementService) {
    this.userCacheManagementService = userCacheManagementService;
  }

  @Override
  public Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId) {
    if (ConnectionExceptionPredicate.isConnectionBasedException.test(pe)) {
      Map<String, Optional<LicenseInfo>> offlineData =
          userCacheManagementService.getDataInOffline(userId);
      Optional<LicenseInfo> cached = offlineData.getOrDefault(userId, Optional.empty());
      if (cached.isPresent()) {
        return cached;
      }
    }
    throw new LicenseServiceUnexpectedException(pe);
  }
}
