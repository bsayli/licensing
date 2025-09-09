package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.LicenseEvaluationService;
import io.github.bsayli.licensing.service.exception.license.LicenseNotFoundException;
import io.github.bsayli.licensing.service.user.orchestration.UserOrchestrationService;
import io.github.bsayli.licensing.service.validation.LicensePolicyValidator;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

@Service
public class LicenseEvaluationServiceImpl implements LicenseEvaluationService {

  private final UserOrchestrationService userService;
  private final LicensePolicyValidator licenseValidationService;

  public LicenseEvaluationServiceImpl(
      UserOrchestrationService userService, LicensePolicyValidator licenseValidationService) {
    this.userService = userService;
    this.licenseValidationService = licenseValidationService;
  }

  @Override
  public LicenseInfo evaluateLicense(IssueTokenRequest request, String userId) {
    return fetchEvaluateAndMaybeRecordUsage(
        userId, request.instanceId(), info -> licenseValidationService.assertValid(info, request));
  }

  @Override
  public LicenseInfo evaluateLicense(ValidateTokenRequest request, String userId) {
    return fetchEvaluateAndMaybeRecordUsage(
        userId, request.instanceId(), info -> licenseValidationService.assertValid(info, request));
  }

  private LicenseInfo fetchEvaluateAndMaybeRecordUsage(
      String userId, String instanceId, Consumer<LicenseInfo> validator) {
    LicenseInfo licenseInfo =
        userService.getLicenseInfo(userId).orElseThrow(() -> new LicenseNotFoundException(userId));

    validator.accept(licenseInfo);

    if (licenseValidationService.isInstanceIdMissing(instanceId, licenseInfo.instanceIds())) {
      userService.recordUsage(userId, instanceId);
    }
    return licenseInfo;
  }
}
