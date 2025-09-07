package io.github.bsayli.licensing.service.user.operations;

import io.github.bsayli.licensing.model.LicenseInfo;
import jakarta.ws.rs.ProcessingException;
import java.util.Optional;

public interface UserService {

  Optional<LicenseInfo> updateLicenseUsage(String userId, String appInstanceId);

  Optional<LicenseInfo> getUser(String userId);

  Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId);
}
