package io.github.bsayli.licensing.service.user;

import io.github.bsayli.licensing.model.LicenseInfo;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UserOrchestrationService {

  Logger logger = LoggerFactory.getLogger(UserOrchestrationService.class);

  void updateLicenseUsage(String userId, String appInstanceId);

  Optional<LicenseInfo> getUser(String userId) throws Exception;
}
