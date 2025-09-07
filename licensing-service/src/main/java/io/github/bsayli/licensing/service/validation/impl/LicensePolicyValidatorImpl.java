package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.model.LicenseInfo;
import io.github.bsayli.licensing.model.LicenseStatus;
import io.github.bsayli.licensing.model.errors.LicenseExpiredException;
import io.github.bsayli.licensing.model.errors.LicenseInactiveException;
import io.github.bsayli.licensing.model.errors.LicenseUsageLimitExceededException;
import io.github.bsayli.licensing.service.validation.LicensePolicyValidator;
import io.github.bsayli.licensing.service.validation.LicenseServicePolicyValidator;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LicensePolicyValidatorImpl implements LicensePolicyValidator {

  private final LicenseServicePolicyValidator detailValidator;

  public LicensePolicyValidatorImpl(LicenseServicePolicyValidator detailValidator) {
    this.detailValidator = detailValidator;
  }

  @Override
  public void assertValid(LicenseInfo licenseInfo, IssueTokenRequest request) {
    assertNotExpired(licenseInfo);
    assertActive(licenseInfo);
    assertWithinUsageLimit(licenseInfo, request.instanceId());
    detailValidator.assertValid(licenseInfo, request);
  }

  @Override
  public void assertValid(LicenseInfo licenseInfo, ValidateTokenRequest request) {
    assertNotExpired(licenseInfo);
    assertActive(licenseInfo);
    assertWithinUsageLimit(licenseInfo, request.instanceId());
    detailValidator.assertValid(licenseInfo, request);
  }

  @Override
  public boolean isInstanceIdMissing(String instanceId, List<String> instanceIds) {
    return CollectionUtils.isEmpty(instanceIds) || !instanceIds.contains(instanceId);
  }

  private void assertNotExpired(LicenseInfo info) {
    if (LocalDateTime.now().isAfter(info.expirationDate())) {
      throw new LicenseExpiredException();
    }
  }

  private void assertActive(LicenseInfo info) {
    if (!LicenseStatus.from(info.licenseStatus()).isActive()) {
      throw new LicenseInactiveException();
    }
  }

  private void assertWithinUsageLimit(LicenseInfo info, String instanceId) {
    boolean within = info.remainingUsageCount() > 0;
    if (!within && isInstanceIdMissing(instanceId, info.instanceIds())) {
      throw new LicenseUsageLimitExceededException(info.maxCount());
    }
  }
}
