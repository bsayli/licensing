package com.c9.licensing.service.user.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.UserCacheManagementService;
import com.c9.licensing.service.user.cache.UserCacheService;
import com.c9.licensing.service.user.operations.UserAsyncService;

@Service
public class UserCacheManagementServiceImpl implements UserCacheManagementService {
	
	private final UserCacheService userOfflineCacheService;
	private final UserCacheService userOnlineCacheService;
	private final UserAsyncService userAsyncService;
	
	public UserCacheManagementServiceImpl(UserCacheService userOfflineCacheService,
			UserCacheService userOnlineCacheService, UserAsyncService userAsyncService) {
		this.userOfflineCacheService = userOfflineCacheService;
		this.userOnlineCacheService = userOnlineCacheService;
		this.userAsyncService = userAsyncService;
	}

	@Override
	public void updateCachesAsync(String userId) throws Exception {
		CompletableFuture<Optional<LicenseInfo>> userAsyncResult = userAsyncService.getUser(userId);
		userAsyncResult.handleAsync((optionalUserInfo, throwable) -> {
		    if (throwable != null) {
		    	logger.error("Processing completed with exception {} for Thread {}", 
		    			throwable.getMessage(),
						Thread.currentThread().getName());
		    } else {
		        userOnlineCacheService.updateUser(userId, optionalUserInfo);
		        userOfflineCacheService.updateUser(userId, optionalUserInfo);
		    }
		    return null;
		});
	}

	@Override
	public Map<String, Optional<LicenseInfo>> getDataInOffline(String userId) {
		return userOfflineCacheService.returnIfExist(userId);
	}

	@Override
	public boolean isOnlineCacheDataExpired(String userId) {
		return !userOnlineCacheService.userExistInCache(userId);
	}

	@Override
	public void refreshDataInOffline(String userId, Optional<LicenseInfo> licenseInfo) {
		userOfflineCacheService.updateUser(userId, licenseInfo);
	}
	
	@Override
	public void refreshDataInCaches(String userId, Optional<LicenseInfo> licenseInfo) {
		userOfflineCacheService.updateUser(userId, licenseInfo);
		userOnlineCacheService.updateUser(userId, licenseInfo);
	}

	@Override
	public void evictDataInCaches(String userId) {
		userOnlineCacheService.evictUser(userId);
		userOfflineCacheService.evictUser(userId);
	}
}
