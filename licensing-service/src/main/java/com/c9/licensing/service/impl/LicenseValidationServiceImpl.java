package com.c9.licensing.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.c9.licensing.errors.LicenseExpiredException;
import com.c9.licensing.errors.LicenseInactiveException;
import com.c9.licensing.errors.LicenseInvalidException;
import com.c9.licensing.errors.LicenseUsageLimitExceededException;
import com.c9.licensing.errors.TokenAlreadyExistException;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.security.EncryptionUtil;
import com.c9.licensing.security.UserIdUtil;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.LicenseValidationService;
import com.c9.licensing.service.user.UserOrchestrationService;

@Service
public class LicenseValidationServiceImpl implements LicenseValidationService {

	private final EncryptionUtil encryptionUtil;
	private final UserIdUtil userIdUtil;
	private final UserOrchestrationService userService;
	private final LicenseClientCacheManagementService clientCacheManagementService;

	public LicenseValidationServiceImpl(EncryptionUtil encryptionUtil, UserIdUtil userIdUtil, 
			UserOrchestrationService userService, LicenseClientCacheManagementService clientCacheManagementService) {
		this.encryptionUtil = encryptionUtil;
		this.userIdUtil = userIdUtil;
		this.userService = userService;
		this.clientCacheManagementService = clientCacheManagementService;
	}

	public LicenseInfo validateLicense(String encLicenseKey, String instanceId) throws Exception {
		String licenseKey = encryptionUtil.decrypt(encLicenseKey);
		String userId = userIdUtil.extractPlainTextUserId(licenseKey);
		
		checkAndThrowIfTokenAlreadyExist(instanceId, userId);
	
		return validateByUserId(userId, instanceId);
	}

	public LicenseInfo validateLicenseForToken(String tokenSubject, String instanceId)
			throws Exception {
		String userId = userIdUtil.decrypt(tokenSubject);
		return validateByUserId(userId, instanceId);
	}

	private LicenseInfo validateByUserId(String userId, String instanceId) throws Exception {
		Optional<LicenseInfo> user = userService.getUser(userId);
		if (user.isPresent()) {
			LicenseInfo licenseInfo = user.get();
			boolean isInstanceIdExist = isInstanceIdExist(instanceId, licenseInfo.instanceIds());
			boolean isWithinUserLimit = isWithinUserLimit(licenseInfo.remainingUsageCount());
			boolean isLicenseExpired = isLicenseExpired(licenseInfo.expirationDate());
			boolean isLicenseStatusActive = isLicenseStatusActive(licenseInfo.licenseStatus());
			if (isLicenseStatusActive && !isLicenseExpired
					&& (isWithinUserLimit || isInstanceIdExist)) {
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
					throw new LicenseUsageLimitExceededException(
							String.format(LICENSE_LIMIT_EXCEEDED, licenseInfo.maxCount()));
				}

			}

		} else {
			throw new LicenseInvalidException("License Key not found or invalid");
		}
	}

	private void checkAndThrowIfTokenAlreadyExist(String instanceId, String userId) throws Exception {
		Optional<String> tokenOpt = clientCacheManagementService.getToken(instanceId);
		if(tokenOpt.isPresent()) {
			String token = tokenOpt.get();
			Optional<String> userIdOpt = clientCacheManagementService.getUserIdByClientId(instanceId);
			if(userIdOpt.isPresent()) {
				String cachedEncUserId = userIdOpt.get();
				String cachedUserId = userIdUtil.decrypt(cachedEncUserId);
				if(userId.equals(cachedUserId)) {
					throw new TokenAlreadyExistException(token, TOKEN_ALREADY_EXIST);
				}	
			}	
		}
	}

	private boolean isLicenseExpired(LocalDateTime expirationDate) {
		return LocalDateTime.now().isAfter(expirationDate);
	}

	private boolean isInstanceIdExist(String instanceId, List<String> instanceIds) {
		return !CollectionUtils.isEmpty(instanceIds) && instanceIds.contains(instanceId);
	}

	private boolean isWithinUserLimit(int remainingUsageCount) {
		return remainingUsageCount > 0;
	}

	private boolean isLicenseStatusActive(String licenseStatus) {
		return "Active".equalsIgnoreCase(licenseStatus);
	}

}