package com.c9.licensing.service.user.impl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.UserService;
import com.c9.licensing.service.user.cache.UserCacheService;
import com.c9.licensing.service.user.operations.UserAsyncService;
import com.c9.licensing.service.user.operations.UserSynchService;

@Service
public class UserServiceImpl implements UserService {

	private final UserSynchService userSynchService;
	private final UserAsyncService userAsyncService;
	private final UserCacheService userOfflineCacheService;
	private final UserCacheService userOnlineCacheService;
	
	public UserServiceImpl(UserSynchService userSynchService, UserAsyncService userAsyncService,
			UserCacheService userOfflineCacheService, UserCacheService userOnlineCacheService) {
		this.userSynchService = userSynchService;
		this.userAsyncService = userAsyncService;
		this.userOfflineCacheService = userOfflineCacheService;
		this.userOnlineCacheService = userOnlineCacheService;
	}

	public Optional<LicenseInfo> getUserSynch(String userId) throws Exception {
		Optional<LicenseInfo> licenseInfo = userSynchService.getUser(userId);
		userOnlineCacheService.cacheUserLicenseInfo(userId, licenseInfo);
		userOfflineCacheService.cacheUserLicenseInfo(userId, licenseInfo);
		return licenseInfo;
	}

	@Override
	public void getUserAsync(String userId) throws Exception {
		boolean isUserCached = userOnlineCacheService.isUserCached(userId);
		if(!isUserCached) {
			CompletableFuture<Optional<LicenseInfo>> userAsyncResult = userAsyncService.getUser(userId);
			userAsyncResult.thenAcceptAsync(optionalUserInfo -> {
				userOnlineCacheService.cacheUserLicenseInfo(userId, optionalUserInfo);
				userOfflineCacheService.cacheUserLicenseInfo(userId, optionalUserInfo);
			});
		}
	}

	@Override
	public void updateLicenseUsage(String userId, String appInstanceId) {
		userSynchService.updateLicenseUsage(userId, appInstanceId);
		userOnlineCacheService.evictUserLicenseInfo(userId);
		userOfflineCacheService.evictUserLicenseInfo(userId);
	}

}
