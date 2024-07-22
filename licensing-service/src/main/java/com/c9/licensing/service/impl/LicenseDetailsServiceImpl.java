package com.c9.licensing.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.errors.LicenseInvalidException;
import com.c9.licensing.errors.TokenAlreadyExistException;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.security.EncryptionUtil;
import com.c9.licensing.security.UserIdUtil;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.LicenseDetailsService;
import com.c9.licensing.service.user.UserOrchestrationService;
import com.c9.licensing.service.validation.LicenseValidationService;

@Service
public class LicenseDetailsServiceImpl implements LicenseDetailsService {

	private final EncryptionUtil encryptionUtil;
	private final UserIdUtil userIdUtil;
	private final UserOrchestrationService userService;
	private final LicenseClientCacheManagementService clientCacheManagementService;
	private final LicenseValidationService licenseValidationService;

	public LicenseDetailsServiceImpl(EncryptionUtil encryptionUtil, UserIdUtil userIdUtil,
			UserOrchestrationService userService, LicenseClientCacheManagementService clientCacheManagementService,
			LicenseValidationService licenseValidationService) {
		this.encryptionUtil = encryptionUtil;
		this.userIdUtil = userIdUtil;
		this.userService = userService;
		this.clientCacheManagementService = clientCacheManagementService;
		this.licenseValidationService = licenseValidationService;
	}

	public LicenseInfo validateAndGetLicenseDetailsByLicenseKey(LicenseValidationRequest request) throws Exception {
		String licenseKey = encryptionUtil.decrypt(request.licenseKey());
		String userId = userIdUtil.extractDecryptedUserId(licenseKey);

		checkAndThrowIfTokenAlreadyExist(request.instanceId(), userId);

		return getValidateAndUpdate(userId, request);
	}

	public LicenseInfo validateAndGetLicenseDetailsByUserId(String encUserId, LicenseValidationRequest request)
			throws Exception {
		String userId = userIdUtil.decrypt(encUserId);
		return getValidateAndUpdate(userId, request);
	}

	private LicenseInfo getValidateAndUpdate(String userId, LicenseValidationRequest request) throws Exception {
		Optional<LicenseInfo> user = userService.getUser(userId);
		if (user.isPresent()) {
			LicenseInfo licenseInfo = user.get();

			licenseValidationService.validate(licenseInfo, request);
			boolean isInstanceIdNotExist = licenseValidationService.isInstanceIdNotExist(request.instanceId(),
					licenseInfo.instanceIds());
			
			if (isInstanceIdNotExist) {
				userService.updateLicenseUsage(userId, request.instanceId());
			}

			return licenseInfo;
		} else {
			throw new LicenseInvalidException("License Key not found or invalid");
		}
	}

	private void checkAndThrowIfTokenAlreadyExist(String instanceId, String userId) throws Exception {
		Optional<String> tokenOpt = clientCacheManagementService.getToken(instanceId);
		if (tokenOpt.isPresent()) {
			String token = tokenOpt.get();
			Optional<String> userIdOpt = clientCacheManagementService.getUserIdByClientId(instanceId);
			if (userIdOpt.isPresent()) {
				String cachedEncUserId = userIdOpt.get();
				String cachedUserId = userIdUtil.decrypt(cachedEncUserId);
				if (userId.equals(cachedUserId)) {
					throw new TokenAlreadyExistException(token, TOKEN_ALREADY_EXIST);
				}
			}
		}
	}

}