package com.c9.licensing.service.user.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.UserOrchestrationService;
import com.c9.licensing.service.user.UserService;
import com.c9.licensing.service.user.cache.UserCacheService;

@Service
public class UserOrchestrationServiceImpl implements UserOrchestrationService {

	private final UserService userService;
	private final UserCacheService userOfflineCacheService;

	public UserOrchestrationServiceImpl(UserService userService,
			UserCacheService userOfflineCacheService) {
		this.userService = userService;
		this.userOfflineCacheService = userOfflineCacheService;
	}

	public Optional<LicenseInfo> getUser(String userId) throws Exception {
		boolean isUserCached = userOfflineCacheService.isUserCached(userId);
		if (isUserCached) {
			Optional<LicenseInfo> cachedLicenseInfo = userOfflineCacheService.getUser(userId);
			userService.getUserAsync(userId);
			return cachedLicenseInfo;
		}
		return userService.getUserSynch(userId);
	}

	@Override
	public void updateLicenseUsage(String userId, String appInstanceId) {
		userService.updateLicenseUsage(userId, appInstanceId);
	}
	
}
