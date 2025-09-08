package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface LicenseServicePolicyValidator {

  void assertValid(LicenseInfo licenseInfo, IssueTokenRequest request);

  void assertValid(LicenseInfo licenseInfo, ValidateTokenRequest request);
}
