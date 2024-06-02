package com.c9.licensing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class TokenCacheService {

	@Autowired
	private CacheManager cacheManager;

	@Cacheable(value = "validTokens", key = "#token", unless = "#result == null")
	public String addValidToken(String token) {
		return token; // Store the token itself as the cached value
	}

	public boolean isTokenValidAndInvalidate(String token) {
		Cache validTokensCache = cacheManager.getCache("validTokens");
		if (validTokensCache != null) {
			Cache.ValueWrapper cachedValue = validTokensCache.get(token);
			if (cachedValue != null) {
				validTokensCache.evict(token);
				return true;
			}
		}
		return false;
	}
}
