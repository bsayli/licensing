package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;

public interface LicenseKeyRequestValidator {

  void assertSignatureValid(IssueAccessRequest request);

  void assertNoConflictingCachedContext(IssueAccessRequest request, String userId);
}
