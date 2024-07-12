package com.c9.licensing.service.impl;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.c9.licensing.service.TokenCacheService;

@Service
public class TokenCacheServiceImpl implements TokenCacheService{

	private CacheManager cacheManager;
	
	public TokenCacheServiceImpl(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	@Cacheable(value = "validTokens", key = "#token", unless = "#result == null")
	public String addValidToken(String token) {
		return token; // Store the token itself as the cached value
	}

	@Override
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
