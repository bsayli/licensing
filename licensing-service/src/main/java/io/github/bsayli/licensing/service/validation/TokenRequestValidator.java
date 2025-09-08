package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;

public interface TokenRequestValidator {
  void assertValid(ValidateTokenRequest request, String token);
}
