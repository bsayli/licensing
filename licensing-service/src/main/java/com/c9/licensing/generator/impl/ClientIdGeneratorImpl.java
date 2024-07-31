package com.c9.licensing.generator.impl;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.c9.licensing.generator.ClientIdGenerator;

@Component
public class ClientIdGeneratorImpl implements ClientIdGenerator {

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
		
		return clientIdBuilder.toString();
	}

}
