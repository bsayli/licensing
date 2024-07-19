package com.c9.licensing.service.impl;

import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.c9.licensing.service.LicenseClientCacheManagementService;

@Service
public class LicenseClientCacheManagementServiceImpl implements LicenseClientCacheManagementService {

	private static final String CACHE_NAME_ACTIVE_CLIENTS_TOKEN = "activeClientsToken";
	private static final String CACHE_NAME_ACTIVE_CLIENTS = "activeClients";

	private final CacheManager cacheManager;

	public LicenseClientCacheManagementServiceImpl(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void addClientInfo(String clientId, String token, String encUserId) {
		Cache activeClientsCache = cacheManager.getCache(CACHE_NAME_ACTIVE_CLIENTS);
		if (activeClientsCache != null) {
			activeClientsCache.put(clientId, token);
		}
		
		Cache activeClientsTokenCache = cacheManager.getCache(CACHE_NAME_ACTIVE_CLIENTS_TOKEN);
		if (activeClientsTokenCache != null) {
			activeClientsTokenCache.put(token, encUserId);
		}
	}
	
	@Override
	public Optional<String> getToken(String clientId) {
		Optional<String> tokenOpt= Optional.empty();
		Cache activeClientsCache = cacheManager.getCache(CACHE_NAME_ACTIVE_CLIENTS);
		if (activeClientsCache != null) {
			Cache.ValueWrapper cachedValueWrapper = activeClientsCache.get(clientId);
			if (cachedValueWrapper != null && cachedValueWrapper.get() != null) {
				tokenOpt = Optional.of((String)cachedValueWrapper.get());
			}
		}
		return tokenOpt;
	}
	
	@Override
	public Optional<String> getUserIdByClientId(String clientId) {
		Optional<String> userIdOpt = Optional.empty();
		Optional<String> tokenOpt = getToken(clientId);
		if(tokenOpt.isPresent()) {
			String token = tokenOpt.get();
			userIdOpt = getUserId(token);
		}
		return userIdOpt;
	}
	
	@Override
	public Optional<String> getUserId(String token) {
		Optional<String> userIdOpt = Optional.empty();
		Cache activeClientsTokenCache = cacheManager.getCache(CACHE_NAME_ACTIVE_CLIENTS_TOKEN);
		if (activeClientsTokenCache != null) {
			Cache.ValueWrapper cachedValueWrapper = activeClientsTokenCache.get(token);
			if (cachedValueWrapper != null && cachedValueWrapper.get() != null) {
				userIdOpt = Optional.of((String)cachedValueWrapper.get());
			}
		}
		return userIdOpt;
	}
	
	@Override
	public Optional<String> getUserIdAndEvictToken(String token) {
		Optional<String> userIdOpt = getUserId(token);
		if(userIdOpt.isPresent()) {
			Cache activeClientsTokenCache = cacheManager.getCache(CACHE_NAME_ACTIVE_CLIENTS_TOKEN);
			if (activeClientsTokenCache != null) {
				activeClientsTokenCache.evict(token);
			}
		}
		return userIdOpt;
	}
	
}