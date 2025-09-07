package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;

public interface LicenseKeyRequestValidationService {

  void validateSignature(IssueTokenRequest request);

  void assertNotCachedWithSameContext(IssueTokenRequest request, String userId);
}
