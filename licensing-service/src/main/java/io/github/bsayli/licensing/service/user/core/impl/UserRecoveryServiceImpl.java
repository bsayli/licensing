package io.github.bsayli.licensing.service.user.core.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.exception.ConnectionExceptionPredicate;
import io.github.bsayli.licensing.service.exception.internal.LicenseServiceInternalException;
import io.github.bsayli.licensing.service.user.core.UserRecoveryService;
import io.github.bsayli.licensing.service.user.orchestration.UserCacheManagementService;
import jakarta.ws.rs.ProcessingException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserRecoveryServiceImpl implements UserRecoveryService {

  private final UserCacheManagementService cache;

  public UserRecoveryServiceImpl(UserCacheManagementService cache) {
    this.cache = cache;
  }

  @Override
  public Optional<LicenseInfo> recoverUser(String userId, ProcessingException cause) {
    if (ConnectionExceptionPredicate.isConnectionBasedException.test(cause)) {
      Optional<LicenseInfo> cached = cache.getOffline(userId);
      if (cached.isPresent()) {
        return cached;
      }
    }
    throw new LicenseServiceInternalException(cause);
  }
}
