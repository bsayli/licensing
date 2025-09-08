package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;

public interface LicenseKeyRequestValidator {

  void assertSignatureValid(IssueTokenRequest request);

  void assertNoConflictingCachedContext(IssueTokenRequest request, String userId);
}
