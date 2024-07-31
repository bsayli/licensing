package com.c9.licensing.service.impl;

import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.c9.licensing.generator.ClientIdGenerator;
import com.c9.licensing.model.ClientCachedLicenseData;
import com.c9.licensing.model.ClientInfo;
import com.c9.licensing.service.LicenseClientCacheManagementService;

@Service
public class LicenseClientCacheManagementServiceImpl implements LicenseClientCacheManagementService {

	private static final String CACHE_NAME_ACTIVE_CLIENTS = "activeClients";

	private final CacheManager cacheManager;
	private final ClientIdGenerator clientIdGenerator;

	public LicenseClientCacheManagementServiceImpl(CacheManager cacheManager, ClientIdGenerator clientIdGenerator) {
		this.cacheManager = cacheManager;
		this.clientIdGenerator = clientIdGenerator;
	}

	@Override
	public void addClientInfo(ClientInfo clientInfo) {
		Cache activeClientsCache = cacheManager.getCache(CACHE_NAME_ACTIVE_CLIENTS);
		if (activeClientsCache != null) {
			String clientId = clientIdGenerator.getClientId(clientInfo.serviceId(), clientInfo.serviceVersion(), clientInfo.instanceId());
			ClientCachedLicenseData clientCachedLicenseData = new ClientCachedLicenseData.Builder()
					.licenseToken(clientInfo.licenseToken())
					.encUserId(clientInfo.encUserId())
					.serviceId(clientInfo.serviceId())
					.serviceVersion(clientInfo.serviceVersion())
					.checksum(clientInfo.checksum())
					.build();

			activeClientsCache.put(clientId, clientCachedLicenseData);
		}
	}

	@Override
	public Optional<ClientCachedLicenseData> getClientCachedLicenseData(String clientId) {
		Optional<ClientCachedLicenseData> tokenOpt = Optional.empty();
		Cache activeClientsCache = cacheManager.getCache(CACHE_NAME_ACTIVE_CLIENTS);
		if (activeClientsCache != null) {
			Cache.ValueWrapper cachedValueWrapper = activeClientsCache.get(clientId);
			if (cachedValueWrapper != null && cachedValueWrapper.get() != null) {
				tokenOpt = Optional.of((ClientCachedLicenseData) cachedValueWrapper.get());
			}
		}
		return tokenOpt;
	}

}