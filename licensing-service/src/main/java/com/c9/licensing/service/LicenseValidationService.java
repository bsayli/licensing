package com.c9.licensing.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.c9.licensing.errors.LicenseExpiredException;
import com.c9.licensing.errors.LicenseInactiveException;
import com.c9.licensing.errors.LicenseInvalidException;
import com.c9.licensing.errors.LicenseUsageLimitExceededException;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.security.EncryptionUtil;
import com.c9.licensing.security.UserIdUtil;

@Service
public class LicenseValidationService {

	private static final String LICENSE_LIMIT_EXCEEDED = "License usage limit exceeded. You can only activate this license on %d machines. " +
	"Please deactivate it on another machine or upgrade your license.";

	private static final String LICENSE_NOT_ACTIVE = "Your license is currently inactive. Please contact support for assistance.";

	private static final String LICENSE_EXPIRED = "Your license has expired. Please renew it to continue using the application.";

	@Autowired
	private EncryptionUtil encryptionUtil;

	@Autowired
	private UserIdUtil userIdUtil;

	@Autowired
	private UserService userService;

	public LicenseInfo validateLicense(String encLicenseKey, String instanceId) throws Exception {

		String licenseKey = encryptionUtil.decrypt(encLicenseKey);

		String userId = userIdUtil.extractUserId(licenseKey);

		return validateByUserId(userId, instanceId);

	}
	
	public LicenseInfo validateLicenseForToken(String tokenSub, String instanceId) throws Exception {
		String userId = userIdUtil.deobfuscateUserId(tokenSub);
		
		return validateByUserId(userId, instanceId);

	}

	private LicenseInfo validateByUserId(String userId, String instanceId) {
		Optional<LicenseInfo> user = userService.getUser(userId);

		if (user.isPresent()) {
			LicenseInfo licenseInfo = user.get();

			boolean isInstanceIdExist = isInstanceIdExist(instanceId, licenseInfo.instanceIds());
			boolean isWithinUserLimit = isWithinUserLimit(licenseInfo.remainingUsageCount());
			boolean isLicenseExpired = isLicenseExpired(licenseInfo.expirationDate());
			boolean isLicenseStatusActive = isLicenseStatusActive(licenseInfo.licenseStatus());
			if (isLicenseStatusActive && !isLicenseExpired && (isWithinUserLimit || isInstanceIdExist)) {

				if (!isInstanceIdExist) {
					userService.updateLicenseUsage(userId, instanceId);
				}

				return licenseInfo;
			} else {
				if (isLicenseExpired) {
				    throw new LicenseExpiredException(LICENSE_EXPIRED);
				} else if (!isLicenseStatusActive) {
				    throw new LicenseInactiveException(LICENSE_NOT_ACTIVE);
				} else {
				    throw new LicenseUsageLimitExceededException(String.format(
				            LICENSE_LIMIT_EXCEEDED,
				            licenseInfo.maxCount()));
				}

			}

		} else {
			throw new LicenseInvalidException("License Key not found or invalid");
		}
	}

	private boolean isLicenseExpired(LocalDateTime expirationDate) {
		return LocalDateTime.now().isAfter(expirationDate);
	}

	private boolean isInstanceIdExist(String instanceId, List<String> instanceIds) {
		if (!CollectionUtils.isEmpty(instanceIds) && instanceIds.contains(instanceId)) {
			return true;
		}
		return false;
	}

	private boolean isWithinUserLimit(int remainingUsageCount) {
		return remainingUsageCount > 0;
	}

	private boolean isLicenseStatusActive(String licenseStatus) {
		return "Active".equalsIgnoreCase(licenseStatus);
	}

}