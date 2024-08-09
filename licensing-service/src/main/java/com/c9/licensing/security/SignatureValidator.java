package com.c9.licensing.security;

import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.errors.SignatureInvalidException;

public interface SignatureValidator {

	String MESSAGE_SIGNATURE_IS_INVALID = "Signature is invalid";
	String ALGORITHM_SHA_256 = "SHA-256";
	String ALGORITHM_DSA = "DSA";
	String ALGORITHM_SHA256WITHDSA = "SHA256withDSA";
	
	void validateSignature(LicenseValidationRequest validationRequest) throws SignatureInvalidException;
}
