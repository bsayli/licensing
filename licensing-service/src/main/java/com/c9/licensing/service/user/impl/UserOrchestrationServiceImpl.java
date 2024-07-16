package com.c9.licensing.service.user.impl;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.UserCacheManagementService;
import com.c9.licensing.service.user.UserOrchestrationService;
import com.c9.licensing.service.user.operations.UserService;

@Service
public class UserOrchestrationServiceImpl implements UserOrchestrationService {

	private final UserService userService;
	private final UserCacheManagementService userCacheManagementService;

	public UserOrchestrationServiceImpl(UserService userService,
			UserCacheManagementService userCacheManagementService) {
		this.userService = userService;
		this.userCacheManagementService = userCacheManagementService;
	}

	public Optional<LicenseInfo> getUser(String userId) throws Exception {
		Map<String, Optional<LicenseInfo>> offlineCacheData = userCacheManagementService.getDataInOffline(userId);
		if (offlineCacheData.containsKey(userId)) {
			if(userCacheManagementService.isOnlineCacheDataExpired(userId)) {
				userCacheManagementService.updateCachesAsync(userId);
			}
			return offlineCacheData.get(userId);
		}
		
	    Optional<LicenseInfo> onlineCacheData = userService.getUser(userId);
	    userCacheManagementService.refreshDataInOffline(userId, onlineCacheData);
	    return onlineCacheData;
	}

	@Override
	public void updateLicenseUsage(String userId, String appInstanceId) {
		Optional<LicenseInfo> updatedLicenseInfo = userService.updateLicenseUsage(userId, appInstanceId);
		userCacheManagementService.refreshDataInCaches(userId, updatedLicenseInfo);
	}
	
}
