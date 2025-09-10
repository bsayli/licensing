package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface LicenseServicePolicyValidator {

  void assertValid(LicenseInfo licenseInfo, IssueAccessRequest request);

  void assertValid(LicenseInfo licenseInfo, ValidateAccessRequest request);
}
