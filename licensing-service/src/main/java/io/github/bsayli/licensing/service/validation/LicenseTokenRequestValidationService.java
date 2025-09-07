package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;

public interface LicenseTokenRequestValidationService {
  void validateTokenRequest(ValidateTokenRequest request, String token);
}
