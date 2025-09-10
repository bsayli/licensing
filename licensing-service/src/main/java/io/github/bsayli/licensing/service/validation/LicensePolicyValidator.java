package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import java.util.List;

public interface LicensePolicyValidator {

  void assertValid(LicenseInfo licenseInfo, IssueAccessRequest request);

  void assertValid(LicenseInfo licenseInfo, ValidateAccessRequest request);

  boolean isInstanceIdMissing(String instanceId, List<String> instanceIds);
}
