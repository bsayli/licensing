package com.c9.licensing.security.impl;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.SignatureData;
import com.c9.licensing.model.SignatureData.Builder;
import com.c9.licensing.model.errors.SignatureInvalidException;
import com.c9.licensing.security.SignatureValidator;

public class SignatureValidatorImpl implements SignatureValidator {

	private final byte[] signaturePublicKey;

	public SignatureValidatorImpl(String signaturePublicKeyStr) {
		this.signaturePublicKey = Base64.getDecoder().decode(signaturePublicKeyStr);
	}

	@Override
	public void validateSignature(LicenseValidationRequest validationRequest)
			throws SignatureInvalidException {
		boolean isValidBase64 = isValidBase64(validationRequest.signature());
		if (!isValidBase64) {
			throw new SignatureInvalidException(MESSAGE_SIGNATURE_IS_INVALID);
		}
		
		try {
			SignatureData signatureData = getSignatureData(validationRequest);
			byte[] hash = calculateSHA256Hash(signatureData.toJson().getBytes());
			Signature signatureObject = Signature.getInstance(ALGORITHM_SHA256WITHDSA);
			signatureObject.initVerify(
					KeyFactory.getInstance(ALGORITHM_DSA).generatePublic(new X509EncodedKeySpec(signaturePublicKey)));
			signatureObject.update(hash);
			boolean isValid = signatureObject.verify(Base64.getDecoder().decode(validationRequest.signature()));
			if (!isValid) {
				throw new SignatureInvalidException(MESSAGE_SIGNATURE_IS_INVALID);
			}
		} catch (Exception e) {
			throw new SignatureInvalidException(MESSAGE_SIGNATURE_IS_INVALID, e);
		}
	}

	private SignatureData getSignatureData(LicenseValidationRequest request) {
		Builder signatureDataBuilder = new SignatureData.Builder().serviceId(request.serviceId())
				.serviceVersion(request.serviceVersion())
				.instanceId(request.instanceId());

		boolean requestedByLicenseKey = request.licenseKey() != null && request.licenseKey().trim().length() > 0;
		if (requestedByLicenseKey) {
			signatureDataBuilder.encryptedLicenseKeyHash(getDataHash(request.licenseKey()));
		} else {
			signatureDataBuilder.licenseTokenHash(getDataHash(request.licenseToken()));
		}
		return signatureDataBuilder.build();
	}

	private String getDataHash(String data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private byte[] calculateSHA256Hash(byte[] data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(ALGORITHM_SHA_256);
			return digest.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new SignatureInvalidException(MESSAGE_SIGNATURE_IS_INVALID, e);
		}
	}

	private boolean isValidBase64(String str) {
		String base64Regex = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
		return str != null && str.matches(base64Regex);
	}
}
