package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;

public interface LicenseOrchestrationService {
  LicenseValidationResponse issueToken(IssueTokenRequest request);

  LicenseValidationResponse validateToken(ValidateTokenRequest request, String bearerToken);
}
