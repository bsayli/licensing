package com.c9.licensing.service.user.cache;

import java.util.Optional;

import com.c9.licensing.model.LicenseInfo;

public interface UserCacheService {
	
	Optional<LicenseInfo> getUser(String userId);

	Optional<LicenseInfo> cacheUserLicenseInfo(String userId, Optional<LicenseInfo> licenseInfo);
	
	void evictUserLicenseInfo(String userId);

	boolean isUserCached(String userId);
}
