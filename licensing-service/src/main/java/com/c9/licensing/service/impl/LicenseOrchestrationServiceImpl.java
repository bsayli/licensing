package com.c9.licensing.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseErrorCode;
import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.response.LicenseValidationResponse;
import com.c9.licensing.security.JwtUtil;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.LicenseOrchestrationService;
import com.c9.licensing.service.LicenseService;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

	private LicenseService licenseService;
	private JwtUtil jwtUtil;
	private LicenseClientCacheManagementService clientCacheManagementService;

	public LicenseOrchestrationServiceImpl(LicenseService licenseService, JwtUtil jwtUtil,
			LicenseClientCacheManagementService clientCacheManagementService) {
		this.licenseService = licenseService;
		this.jwtUtil = jwtUtil;
		this.clientCacheManagementService = clientCacheManagementService;
	}

	@Override
	public LicenseValidationResponse getLicenseDetails(String licenseKey, String instanceId) {
		LicenseValidationResponse licenseValidationResponse;
		LicenseValidationResult result = licenseService.getUserLicenseDetails(licenseKey, instanceId);
		if (result.valid()) {
			String token = jwtUtil.generateToken(result);
			clientCacheManagementService.addClientInfo(instanceId, token, result.userId());
			licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
					.token(token)
					.message(result.message())
					.build();
		} else {
			licenseValidationResponse = new LicenseValidationResponse.Builder().success(false)
					.errorCode(result.errorCode().name())
					.message(result.message())
					.build();
		}

		return licenseValidationResponse;
	}

	@Override
	public LicenseValidationResponse getLicenseDetailsByToken(String token, String instanceId) {
		LicenseValidationResponse licenseValidationResponse;
		LicenseValidationResult result = licenseService.getUserLicenseDetailsByToken(token, instanceId);
		if (result.valid()) {
			if (LicenseErrorCode.TOKEN_REFRESHED == result.errorCode()) {
				String newToken = jwtUtil.generateToken(result);
				clientCacheManagementService.addClientInfo(instanceId, newToken, result.userId());
				licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
						.token(newToken)
						.errorCode(result.errorCode().name())
						.message(result.message())
						.build();
			} else {
				licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
						.token(token)
						.message(result.message())
						.build();
			}

		} else {
			licenseValidationResponse = new LicenseValidationResponse.Builder().success(false)
					.errorCode(result.errorCode().name())
					.message(result.message())
					.build();
		}
		return licenseValidationResponse;
	}

	private Optional<String> getCachedTokenIfAlreadyExist(String instanceId, LicenseValidationResult result) {
		Optional<String> tokenOpt = Optional.empty();
		if (LicenseErrorCode.TOKEN_ALREADY_EXIST == result.errorCode()) {
			tokenOpt = clientCacheManagementService.getToken(instanceId);
		}
		return tokenOpt;
	}

}
