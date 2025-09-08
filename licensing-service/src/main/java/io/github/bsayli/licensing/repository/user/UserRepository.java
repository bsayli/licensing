package io.github.bsayli.licensing.repository.user;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import java.util.Optional;

public interface UserRepository {

  Optional<LicenseInfo> updateLicenseUsage(String userId, String appInstanceId);

  Optional<LicenseInfo> getUser(String userId);
}
