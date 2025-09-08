package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.domain.model.LicenseInfo;

public interface LicenseEvaluationService {

  LicenseInfo evaluateLicense(IssueTokenRequest request, String userId);

  LicenseInfo evaluateLicense(ValidateTokenRequest request, String userId);
}
