package io.github.bsayli.licensing.service.user.core;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import jakarta.ws.rs.ProcessingException;

public interface UserRecoveryService {
  LicenseInfo recoverUser(String userId, ProcessingException cause);
}
