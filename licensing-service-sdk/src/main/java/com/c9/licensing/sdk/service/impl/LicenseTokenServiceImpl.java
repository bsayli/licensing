package com.c9.licensing.sdk.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.c9.licensing.sdk.service.LicenseTokenService;

@Service
public class LicenseTokenServiceImpl implements LicenseTokenService {

	private final ConcurrentHashMap<String, String> licenseTokenMap = new ConcurrentHashMap<>();

	@Override
	public void storeLicenseToken(String clientId, String licenseToken) {
		licenseTokenMap.put(clientId, licenseToken);
	}

	@Override
	public String getLicenseToken(String clientId) {
		return licenseTokenMap.get(clientId);
	}
	
	@Override
	public void removeLicenseToken(String clientId) {
		licenseTokenMap.remove(clientId);
	}
}
