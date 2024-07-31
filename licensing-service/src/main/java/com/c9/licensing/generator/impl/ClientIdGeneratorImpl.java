package com.c9.licensing.generator.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.c9.licensing.generator.ClientIdGenerator;

@Component
public class ClientIdGeneratorImpl implements ClientIdGenerator {
	
	private static final String ALGORITHM = "SHA-256";

	public String getClientId(String serviceId, String serviceVersion, String instanceId) {
		StringBuilder clientIdBuilder = new StringBuilder();

		if (Objects.nonNull(serviceId)) {
			clientIdBuilder.append(serviceId);
		}

		if (Objects.nonNull(serviceVersion)) {
			clientIdBuilder.append(serviceVersion);
		}

		if (Objects.nonNull(instanceId)) {
			clientIdBuilder.append(instanceId);
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
