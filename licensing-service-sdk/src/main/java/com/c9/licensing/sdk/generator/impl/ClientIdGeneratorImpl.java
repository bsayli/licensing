package com.c9.licensing.sdk.generator.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.c9.licensing.sdk.generator.ClientIdGenerator;
import com.c9.licensing.sdk.model.LicenseValidationRequest;

@Component
public class ClientIdGeneratorImpl implements ClientIdGenerator {
	
	private static final String ALGORITHM = "SHA-256";

	@Override
	public String getClientId(LicenseValidationRequest request) {
		StringBuilder clientIdBuilder = new StringBuilder();
		clientIdBuilder.append(request.licenseKey());
		clientIdBuilder.append(request.serviceId());
		clientIdBuilder.append(request.serviceVersion());
		clientIdBuilder.append(request.instanceId());
		
		if (Objects.nonNull(request.checksum())) {
			clientIdBuilder.append(request.checksum());
		}

		try {
			MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
			byte[] hashedBytes = digest.digest(clientIdBuilder.toString().getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hashedBytes);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	
	}

}
