package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.LicenseInfo;
import io.github.bsayli.licensing.model.errors.LicenseExpiredException;
import io.github.bsayli.licensing.model.errors.LicenseInactiveException;
import io.github.bsayli.licensing.model.errors.LicenseUsageLimitExceededException;
import io.github.bsayli.licensing.service.validation.LicenseResultServiceDetailValidationService;
import io.github.bsayli.licensing.service.validation.LicenseResultValidationService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LicenseResultValidationServiceImpl implements LicenseResultValidationService {

  private final LicenseResultServiceDetailValidationService serviceDetailValidationService;

  public LicenseResultValidationServiceImpl(
      LicenseResultServiceDetailValidationService serviceDetailValidationService) {
    this.serviceDetailValidationService = serviceDetailValidationService;
  }

  @Override
  public void validate(LicenseInfo licenseInfo, LicenseValidationRequest request) {
    validateLicenseExpiration(licenseInfo);
    validateLicenseStatus(licenseInfo);
    validateUsageLimit(licenseInfo, request);
    serviceDetailValidationService.validate(licenseInfo, request);
  }

  @Override
  public boolean isInstanceIdNotExist(String instanceId, List<String> instanceIds) {
    return CollectionUtils.isEmpty(instanceIds) || !instanceIds.contains(instanceId);
  }

  private void validateLicenseExpiration(LicenseInfo licenseInfo) {
    if (isLicenseExpired(licenseInfo.expirationDate())) {
      throw new LicenseExpiredException(MESSAGE_LICENSE_EXPIRED);
    }
  }

  private void validateLicenseStatus(LicenseInfo licenseInfo) {
    if (!isLicenseStatusActive(licenseInfo.licenseStatus())) {
      throw new LicenseInactiveException(MESSAGE_LICENSE_NOT_ACTIVE);
    }
  }

  private void validateUsageLimit(LicenseInfo licenseInfo, LicenseValidationRequest request) {
    if (!isWithinUserLimit(licenseInfo.remainingUsageCount())
        && isInstanceIdNotExist(request.instanceId(), licenseInfo.instanceIds())) {
      throw new LicenseUsageLimitExceededException(
          String.format(MESSAGE_LICENSE_LIMIT_EXCEEDED, licenseInfo.maxCount()));
    }
  }

  private boolean isWithinUserLimit(int remainingUsageCount) {
    return remainingUsageCount > 0;
  }

  private boolean isLicenseExpired(LocalDateTime expirationDate) {
    return LocalDateTime.now().isAfter(expirationDate);
  }

  private boolean isLicenseStatusActive(String licenseStatus) {
    return "Active".equalsIgnoreCase(licenseStatus);
  }
}
