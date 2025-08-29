package com.c9.licensing.service.user;

import com.c9.licensing.model.LicenseInfo;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UserOrchestrationService {

  Logger logger = LoggerFactory.getLogger(UserOrchestrationService.class);

  void updateLicenseUsage(String userId, String appInstanceId);

  Optional<LicenseInfo> getUser(String userId) throws Exception;
}
