package com.c9.licensing.service.impl;

import org.springframework.stereotype.Service;

import com.c9.licensing.model.ClientInfo;
import com.c9.licensing.model.LicenseServiceStatus;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.model.errors.InvalidParameterException;
import com.c9.licensing.model.response.LicenseValidationResponse;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.LicenseOrchestrationService;
import com.c9.licensing.service.LicenseService;
import com.c9.licensing.service.jwt.JwtService;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

	private LicenseService licenseService;
	private JwtService jwtUtil;
	private LicenseClientCacheManagementService clientCacheManagementService;

	public LicenseOrchestrationServiceImpl(LicenseService licenseService, JwtService jwtUtil,
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
			ClientInfo clientInfo = new ClientInfo.Builder().serviceId(request.serviceId())
					.licenseToken(token)
					.serviceVersion(request.serviceVersion())
					.instanceId(request.instanceId())
					.encUserId(result.userId())
					.checksum(request.checksum())
					.signature(request.signature())
					.build();
			clientCacheManagementService.addClientInfo(clientInfo);
			licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
					.licenseToken(token)
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
				ClientInfo clientInfo = new ClientInfo.Builder().serviceId(request.serviceId())
						.licenseToken(newToken)
						.serviceVersion(request.serviceVersion())
						.instanceId(request.instanceId())
						.encUserId(result.userId())
						.checksum(request.checksum())
						.signature(request.signature())
						.build();
				clientCacheManagementService.addClientInfo(clientInfo);
				licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
						.licenseToken(newToken)
						.status(result.serviceStatus().name())
						.message(result.message())
						.build();
			} else {
				licenseValidationResponse = new LicenseValidationResponse.Builder().success(true)
						.licenseToken(request.licenseToken())
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

}
