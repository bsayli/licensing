package com.c9.licensing.service.user.cache;

import java.util.Map;
import java.util.Optional;

import com.c9.licensing.model.LicenseInfo;

public interface UserCacheService {
	
	Optional<LicenseInfo> getUser(String userId);

	Optional<LicenseInfo> addUser(String userId, Optional<LicenseInfo> licenseInfo);
	
	void updateUser(String userId, Optional<LicenseInfo> licenseInfo);
	
	void evictUser(String userId);

	boolean userExistInCache(String userId);
	
	Map<String, Optional<LicenseInfo>> returnIfExist(String userId);
}
