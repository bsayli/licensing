package io.github.bsayli.licensing.service.user.orchestration;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import java.util.Optional;

public interface UserOrchestrationService {

  void recordUsage(String userId, String instanceId);

  Optional<LicenseInfo> getLicenseInfo(String userId);
}
