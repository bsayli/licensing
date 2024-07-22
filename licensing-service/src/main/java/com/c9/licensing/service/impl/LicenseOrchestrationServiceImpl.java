package com.c9.licensing.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.errors.InvalidParameterException;
import com.c9.licensing.model.LicenseServiceStatus;
import com.c9.licensing.model.LicenseValidationRequest;
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
	public LicenseValidationResponse getLicenseDetails(LicenseValidationRequest request) {

		validateParameterPresence(request.licenseKey(), request.licenseToken());

		if (request.licenseKey() != null) {
			return getLicenseDetailsByLicenseKey(request);
		} else {
			return getLicenseDetailsByToken(request);
		}
	}

	@Override
	public LicenseValidationResponse getLicenseDetailsByLicenseKey(LicenseValidationRequest request) {
		LicenseValidationResponse licenseValidationResponse;
		LicenseValidationResult result = licenseService.getUserLicenseDetailsByLicenseKey(request);
		if (result.valid()) {
			String token = jwtUtil.generateToken(result);
			clientCacheManagementService.addClientInfo(request.instanceId(), token, result.userId());
			licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
					.token(token)
					.status(LicenseServiceStatus.TOKEN_CREATED.name())
					.message(result.message())
					.build();
		} else {
			licenseValidationResponse = new LicenseValidationResponse.Builder().success(false)
					.status(result.serviceStatus().name())
					.message(result.message())
					.build();
		}

		return licenseValidationResponse;
	}

	@Override
	public LicenseValidationResponse getLicenseDetailsByToken(LicenseValidationRequest request) {
		LicenseValidationResponse licenseValidationResponse;
		LicenseValidationResult result = licenseService.getUserLicenseDetailsByToken(request);
		if (result.valid()) {
			if (LicenseServiceStatus.TOKEN_REFRESHED == result.serviceStatus()) {
				String newToken = jwtUtil.generateToken(result);
				clientCacheManagementService.addClientInfo(request.instanceId(), newToken, result.userId());
				licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
						.token(newToken)
						.status(result.serviceStatus().name())
						.message(result.message())
						.build();
			} else {
				licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
						.token(request.licenseToken())
						.status(LicenseServiceStatus.TOKEN_ACTIVE.name())
						.message(result.message())
						.build();
			}

		} else {
			licenseValidationResponse = new LicenseValidationResponse.Builder().success(false)
					.status(result.serviceStatus().name())
					.message(result.message())
					.build();
		}
		return licenseValidationResponse;
	}
	
	private void validateParameterPresence(String licenseKey, String licenseToken) {
	    if (licenseKey == null && licenseToken == null) {
	        throw new InvalidParameterException("Either licenseKey or licenseToken is required");
	    }
	    if (licenseKey != null && licenseToken != null) {
	        throw new InvalidParameterException("Only one of licenseKey or licenseToken can be provided");
	    }
	}
	
	private Optional<String> getCachedTokenIfAlreadyExist(String instanceId, LicenseValidationResult result) {
		Optional<String> tokenOpt = Optional.empty();
		if (LicenseServiceStatus.TOKEN_ALREADY_EXIST == result.serviceStatus()) {
			tokenOpt = clientCacheManagementService.getToken(instanceId);
		}
		return tokenOpt;
	}

}
