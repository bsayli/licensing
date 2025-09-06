package io.github.bsayli.licensing.security;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.errors.SignatureInvalidException;

public interface SignatureValidator {

  String MESSAGE_SIGNATURE_IS_INVALID = "Signature is invalid";
  String ALGORITHM_SHA_256 = "SHA-256";
  String ALGORITHM_DSA = "DSA";
  String ALGORITHM_SHA256WITHDSA = "SHA256withDSA";

  void validateSignature(LicenseValidationRequest validationRequest)
      throws SignatureInvalidException;
}
