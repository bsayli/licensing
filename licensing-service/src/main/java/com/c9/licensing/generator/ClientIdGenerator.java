package com.c9.licensing.generator;

public interface ClientIdGenerator {
	
	String getClientId(String serviceId, String serviceVersion, String instanceId);

}
