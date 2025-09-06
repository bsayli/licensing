package io.github.bsayli.licensing.service.impl;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.LicenseInfo;
import io.github.bsayli.licensing.model.errors.LicenseInvalidException;
import io.github.bsayli.licensing.service.LicenseDetailsService;
import io.github.bsayli.licensing.service.user.UserOrchestrationService;
import io.github.bsayli.licensing.service.validation.LicenseResultValidationService;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LicenseDetailsServiceImpl implements LicenseDetailsService {

  private final UserOrchestrationService userService;
  private final LicenseResultValidationService licenseValidationService;

  public LicenseDetailsServiceImpl(
      UserOrchestrationService userService,
      LicenseResultValidationService licenseValidationService) {
    this.userService = userService;
    this.licenseValidationService = licenseValidationService;
  }

  public LicenseInfo getAndValidateLicenseDetails(LicenseValidationRequest request, String userId)
      throws Exception {
    Optional<LicenseInfo> user = userService.getUser(userId);
    if (user.isPresent()) {
      LicenseInfo licenseInfo = user.get();

      licenseValidationService.validate(licenseInfo, request);
      boolean isInstanceIdNotExist =
          licenseValidationService.isInstanceIdNotExist(
              request.instanceId(), licenseInfo.instanceIds());

      if (isInstanceIdNotExist) {
        userService.updateLicenseUsage(userId, request.instanceId());
      }

      return licenseInfo;
    } else {
      throw new LicenseInvalidException("License Key not found or invalid");
    }
  }
}
