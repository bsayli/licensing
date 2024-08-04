package com.c9.licensing.generator.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.c9.licensing.generator.ClientIdGenerator;
import com.c9.licensing.model.ClientInfo;
import com.c9.licensing.model.LicenseValidationRequest;

@Component
public class ClientIdGeneratorImpl implements ClientIdGenerator {

	private static final String ALGORITHM = "SHA-256";

	@Override
	public String getClientId(LicenseValidationRequest request) {
		return getClientId(request.serviceId(), request.serviceVersion(), request.checksum(), request.instanceId());
	}

	@Override
	public String getClientId(ClientInfo clientInfo) {
		return getClientId(clientInfo.serviceId(), clientInfo.serviceVersion(), clientInfo.checksum(),
				clientInfo.instanceId());
	}

	private String getClientId(String serviceId, String serviceVersion, String checksum, String instanceId) {
		StringBuilder clientIdBuilder = new StringBuilder();
		clientIdBuilder.append(serviceId);
		clientIdBuilder.append(serviceVersion);

		if (Objects.nonNull(checksum)) {
			clientIdBuilder.append(checksum);
		}

		clientIdBuilder.append(instanceId);

		try {
			MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
			byte[] hashedBytes = digest.digest(clientIdBuilder.toString().getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hashedBytes);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

}
