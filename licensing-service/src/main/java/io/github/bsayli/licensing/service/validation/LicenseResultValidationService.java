package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.LicenseInfo;
import java.util.List;

public interface LicenseResultValidationService {

  String MESSAGE_LICENSE_LIMIT_EXCEEDED =
      "License usage limit exceeded. You can only activate this license on %d machines. "
          + "Please deactivate it on another machine or upgrade your license.";

  String MESSAGE_LICENSE_NOT_ACTIVE =
      "Your license is currently inactive. Please contact support for assistance.";

  String MESSAGE_LICENSE_EXPIRED =
      "Your license has expired. Please renew it to continue using the application.";

  void validate(LicenseInfo licenseInfo, LicenseValidationRequest request);

  boolean isInstanceIdNotExist(String instanceId, List<String> instanceIds);
}
