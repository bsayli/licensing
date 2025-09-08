package io.github.bsayli.licensing.service.user.operations;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import jakarta.ws.rs.ProcessingException;
import java.util.Optional;

public interface UserRecoveryService {
  Optional<LicenseInfo> recoverUser(String userId, ProcessingException cause);
}
