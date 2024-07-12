package com.c9.licensing.service.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c9.licensing.model.LicenseInfo;

public interface UserService {
	
	Logger logger = LoggerFactory.getLogger(UserService.class);

	void updateLicenseUsage(String userId, String appInstanceId);

	Optional<LicenseInfo> getUserSynch(String userId) throws Exception;
	
	void getUserAsync(String userId) throws Exception;

}
