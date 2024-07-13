package com.c9.licensing.service.user;

import java.util.Map;
import java.util.Optional;

import com.c9.licensing.model.LicenseInfo;

public interface UserCacheManagementService {
	
	void updateCachesAsync(String userId) throws Exception;
	
	Map<String, Optional<LicenseInfo>> getDataInOffline(String userId);
	
	boolean isOnlineCacheDataExpired(String userId);
	
	void refreshDataInOffline(String userId, Optional<LicenseInfo> licenseInfo);
	
	void evictDataInCaches(String userId);

}
