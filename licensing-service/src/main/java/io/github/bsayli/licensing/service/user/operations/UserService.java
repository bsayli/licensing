package io.github.bsayli.licensing.service.user.operations;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import java.util.Optional;

public interface UserService {

  Optional<LicenseInfo> updateLicenseUsage(String userId, String instanceId);

  Optional<LicenseInfo> getUser(String userId);
}
