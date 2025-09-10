package io.github.bsayli.licensing.service;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;

public interface LicenseOrchestrationService {
  LicenseAccessResponse issueAccess(IssueAccessRequest request);

  LicenseAccessResponse validateAccess(ValidateAccessRequest request, String bearerToken);
}
