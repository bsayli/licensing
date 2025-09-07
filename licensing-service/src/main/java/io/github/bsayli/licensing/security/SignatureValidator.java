package io.github.bsayli.licensing.security;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.model.errors.SignatureInvalidException;

public interface SignatureValidator {

  String ALGORITHM_SHA_256 = "SHA-256";
  String ALGORITHM_DSA = "DSA";
  String ALGORITHM_SHA256WITHDSA = "SHA256withDSA";

  void validate(IssueTokenRequest request) throws SignatureInvalidException;

  void validate(ValidateTokenRequest request, String token) throws SignatureInvalidException;
}
