package com.c9.licensing.service.user.cache.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.cache.UserCacheService;

@Service("userOnlineCacheService")
public class UserOnlineCacheServiceImpl implements UserCacheService{
	
	private static final String CACHE_NAME_USER_INFO = "userInfoCache";

	private final Logger logger = LoggerFactory.getLogger(UserOnlineCacheServiceImpl.class);
	
	private CacheManager cacheManager;
	
	public UserOnlineCacheServiceImpl(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public Optional<LicenseInfo> getUser(String userId) {
		Optional<LicenseInfo> userInfoOpt = Optional.empty();
		Cache userOfflineInfoCache = cacheManager.getCache(CACHE_NAME_USER_INFO);
		if (userOfflineInfoCache != null) {
			Cache.ValueWrapper cachedUserInfoValue = userOfflineInfoCache.get(userId);
			if (cachedUserInfoValue != null) {
				userInfoOpt = Optional.ofNullable((LicenseInfo)cachedUserInfoValue.get());
			}
		}
		return userInfoOpt;
	}
	
	@Override
	public boolean isUserCached(String userId) {
		boolean isUserCached = false;
		Cache userOfflineInfoCache = cacheManager.getCache(CACHE_NAME_USER_INFO);
		if (userOfflineInfoCache != null) {
			Cache.ValueWrapper cachedUserInfoValue = userOfflineInfoCache.get(userId);
			if (cachedUserInfoValue != null) {
				isUserCached = true;
			}
		}
		return isUserCached;
	}

	@Override
	@Cacheable(value = CACHE_NAME_USER_INFO, key = "#userId")
	public Optional<LicenseInfo> cacheUserLicenseInfo(String userId, Optional<LicenseInfo> licenseInfo){
		logger.info("userLicenseInfo is cached for online service {}", licenseInfo);
		return licenseInfo; // Store the licenseInFo itself as the cached value
	}
	
	@Override
	@CacheEvict(cacheNames = CACHE_NAME_USER_INFO, key = "#userId")
	public void evictUserLicenseInfo(String userId){
	}

}
