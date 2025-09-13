package io.github.bsayli.licensing.service.user.core.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.exception.ConnectionExceptionPredicate;
import io.github.bsayli.licensing.service.exception.internal.LicenseServiceInternalException;
import io.github.bsayli.licensing.service.user.core.UserRecoveryService;
import io.github.bsayli.licensing.service.user.orchestration.UserCacheManagementService;
import jakarta.ws.rs.ProcessingException;
import org.springframework.stereotype.Service;

@Service
public class UserRecoveryServiceImpl implements UserRecoveryService {

  private final UserCacheManagementService cache;

  public UserRecoveryServiceImpl(UserCacheManagementService cache) {
    this.cache = cache;
  }

  @Override
  public LicenseInfo recoverUser(String userId, ProcessingException cause) {
    if (ConnectionExceptionPredicate.isConnectionBasedException.test(cause)) {
      LicenseInfo cached = cache.getOffline(userId);
      if (cached != null) {
        return cached;
      }
      throw new LicenseServiceInternalException(cause, "Offline cache miss for user " + userId);
    }
    throw new LicenseServiceInternalException(cause);
  }
}
