package io.github.bsayli.licensing.service.user.operations;

import io.github.bsayli.licensing.model.LicenseInfo;
import jakarta.ws.rs.ProcessingException;
import java.util.Optional;

public interface UserRecoverService {

  Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId);
}
