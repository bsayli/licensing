package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface LicenseEvaluationService {

  LicenseInfo evaluateLicense(IssueAccessRequest request, String userId);

  LicenseInfo evaluateLicense(ValidateAccessRequest request, String userId);
}
