package com.c9.licensing.service.user.cache.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.cache.UserCacheService;

@Service("userOfflineCacheService")
public class UserOfflineCacheServiceImpl implements UserCacheService {
	
	private static final String CACHE_NAME_USER_OFFLINE_INFO = "userOfflineInfoCache";
	
	private CacheManager cacheManager;
	
	public UserOfflineCacheServiceImpl(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	@Override
	@Cacheable(value = CACHE_NAME_USER_OFFLINE_INFO, key = "#userId")
	public Optional<LicenseInfo> addUser(String userId, Optional<LicenseInfo> licenseInfo){
		return licenseInfo; 
	}
	
	@Override
	@CacheEvict(cacheNames = CACHE_NAME_USER_OFFLINE_INFO, key = "#userId")
	public void evictUser(String userId){
	}

	@Override
	public void updateUser(String userId, Optional<LicenseInfo> licenseInfo) {
		Cache userOfflineInfoCache = cacheManager.getCache(CACHE_NAME_USER_OFFLINE_INFO);
		if (userOfflineInfoCache != null) {
			userOfflineInfoCache.put(userId, licenseInfo);
		}
	}

	@Override
	public Optional<LicenseInfo> getUser(String userId) {
		Optional<LicenseInfo> userInfoOpt = Optional.empty();
		Cache userOfflineInfoCache = cacheManager.getCache(CACHE_NAME_USER_OFFLINE_INFO);
		if (userOfflineInfoCache != null) {
			Cache.ValueWrapper cachedValueWrapper = userOfflineInfoCache.get(userId);
			if (cachedValueWrapper != null && cachedValueWrapper.get() != null) {
				Object object = cachedValueWrapper.get();
				if(object != null && object.getClass().isAssignableFrom(Optional.class)) {
					Optional<?> objectOptional = (Optional<?>)object;
					if(objectOptional.isPresent()) {
						return Optional.ofNullable((LicenseInfo) objectOptional.get());
					}
				}
			}
		}
		return userInfoOpt;
	}
	
	@Override
	public boolean userExistInCache(String userId) {
		boolean isUserCached = false;
		Cache userOfflineInfoCache = cacheManager.getCache(CACHE_NAME_USER_OFFLINE_INFO);
		if (userOfflineInfoCache != null) {
			Cache.ValueWrapper cachedUserInfoValue = userOfflineInfoCache.get(userId);
			if (cachedUserInfoValue != null) {
				isUserCached = true;
			}
		}
		return isUserCached;
	}

	@Override
	public Map<String, Optional<LicenseInfo>> returnIfExist(String userId) {
		Map<String, Optional<LicenseInfo>> dataMap = new HashMap<>();
		Optional<LicenseInfo> licenseInfo = getUser(userId);
		boolean exist = userExistInCache(userId);
		if(exist) {
			dataMap.put(userId, licenseInfo);
		}
		return dataMap;
	}
}
