package io.github.bsayli.licensing.security;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.service.exception.security.SignatureInvalidException;

public interface SignatureValidator {

  void validate(IssueAccessRequest request) throws SignatureInvalidException;

  void validate(ValidateAccessRequest request, String token) throws SignatureInvalidException;
}
