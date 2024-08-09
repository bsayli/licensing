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
		return getClientId(request.instanceId(), request.serviceId(), request.serviceVersion(), request.checksum());
	}

	@Override
	public String getClientId(ClientInfo clientInfo) {
		return getClientId(clientInfo.instanceId(), clientInfo.serviceId(), clientInfo.serviceVersion(), clientInfo.checksum());
	}

	private String getClientId(String instanceId, String serviceId, String serviceVersion, String checksum) {
		StringBuilder clientIdBuilder = new StringBuilder();
		clientIdBuilder.append(instanceId);
		clientIdBuilder.append(serviceId);
		clientIdBuilder.append(serviceVersion);
		
		if (Objects.nonNull(checksum)) {
			clientIdBuilder.append(checksum);
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
