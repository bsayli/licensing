package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;

public interface TokenRequestValidator {
  void assertValid(ValidateAccessRequest request, String token);
}
