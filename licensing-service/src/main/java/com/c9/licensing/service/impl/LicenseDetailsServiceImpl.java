package com.c9.licensing.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.errors.LicenseInvalidException;
import com.c9.licensing.service.LicenseDetailsService;
import com.c9.licensing.service.user.UserOrchestrationService;
import com.c9.licensing.service.validation.LicenseValidationService;

@Service
public class LicenseDetailsServiceImpl implements LicenseDetailsService {

	private final UserOrchestrationService userService;
	private final LicenseValidationService licenseValidationService;

	public LicenseDetailsServiceImpl(UserOrchestrationService userService,
			LicenseValidationService licenseValidationService) {
		this.userService = userService;
		this.licenseValidationService = licenseValidationService;
	}

	public LicenseInfo getAndValidateLicenseDetails(LicenseValidationRequest request, String userId) throws Exception {
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

}