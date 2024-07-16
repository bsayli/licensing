package com.c9.licensing.service.user.repository;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c9.licensing.model.LicenseInfo;

public interface UserRepository {

	Logger logger = LoggerFactory.getLogger(UserRepository.class);

	String ATTR_INSTANCE_IDS = "instanceIds";
	String ATTR_REMAINING_USAGE_COUNT = "remainingUsageCount";
	String ATTR_MAX_COUNT = "maxCount";
	String ATTR_LICENSE_EXPIRATION = "licenseExpiration";
	String ATTR_LICENSE_STATUS = "licenseStatus";
	String ATTR_LICENSE_TIER = "licenseTier";

	Optional<LicenseInfo> updateLicenseUsage(String userId, String appInstanceId);

	Optional<LicenseInfo> getUser(String userId);

}
