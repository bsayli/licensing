package com.c9.licensing.service.impl;

import org.springframework.stereotype.Service;

import com.c9.licensing.generator.ClientIdGenerator;
import com.c9.licensing.model.ClientInfo;
import com.c9.licensing.model.LicenseServiceStatus;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.model.errors.InvalidParameterException;
import com.c9.licensing.model.response.LicenseValidationResponse;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.LicenseOrchestrationService;
import com.c9.licensing.service.LicenseService;
import com.c9.licensing.service.jwt.JwtBlacklistService;
import com.c9.licensing.service.jwt.JwtService;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

	private final LicenseService licenseService;
	private final JwtService jwtUtil;
	private final LicenseClientCacheManagementService clientCacheManagementService;
	private final ClientIdGenerator clientIdGenerator;
	private final JwtBlacklistService jwtBlacklistService;

	public LicenseOrchestrationServiceImpl(LicenseService licenseService, JwtService jwtUtil,
			LicenseClientCacheManagementService clientCacheManagementService, ClientIdGenerator clientIdGenerator,
			JwtBlacklistService jwtBlacklistService) {
		this.licenseService = licenseService;
		this.jwtUtil = jwtUtil;
		this.clientCacheManagementService = clientCacheManagementService;
		this.clientIdGenerator = clientIdGenerator;
		this.jwtBlacklistService = jwtBlacklistService;
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

			String clientId = clientIdGenerator.getClientId(request.serviceId(), request.serviceVersion(),
					request.instanceId());

			if (request.forceTokenRefresh()) {
				jwtBlacklistService.addCurrentTokenToBlacklist(clientId);
			}

			String token = generateTokenAndAddToCache(clientId, request, result);

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
				String clientId = clientIdGenerator.getClientId(request.serviceId(), request.serviceVersion(),
						request.instanceId());
				String newToken = generateTokenAndAddToCache(clientId, request, result);
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

	private String generateTokenAndAddToCache(String clientId, LicenseValidationRequest request,
			LicenseValidationResult result) {
		String token = jwtUtil.generateToken(clientId, result.licenseTier(), result.licenseStatus());
		ClientInfo clientInfo = new ClientInfo.Builder().serviceId(request.serviceId())
				.licenseToken(token)
				.serviceVersion(request.serviceVersion())
				.instanceId(request.instanceId())
				.encUserId(result.userId())
				.checksum(request.checksum())
				.signature(request.signature())
				.build();
		clientCacheManagementService.addClientInfo(clientInfo);
		return token;
	}

}
