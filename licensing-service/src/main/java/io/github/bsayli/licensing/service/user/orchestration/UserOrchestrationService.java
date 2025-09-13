package io.github.bsayli.licensing.service.user.orchestration;

import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface UserOrchestrationService {

  void recordUsage(String userId, String instanceId);

  LicenseInfo getLicenseInfo(String userId);
}
