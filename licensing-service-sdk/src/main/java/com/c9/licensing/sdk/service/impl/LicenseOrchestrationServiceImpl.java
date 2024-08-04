package com.c9.licensing.sdk.service.impl;

import static com.c9.licensing.sdk.model.server.LicenseServerServiceStatus.TOKEN_CREATED;
import static com.c9.licensing.sdk.model.server.LicenseServerServiceStatus.TOKEN_REFRESHED;
import static com.c9.licensing.sdk.model.server.LicenseServerServiceStatus.TOKEN_ACTIVE;

import org.springframework.stereotype.Service;

import com.c9.licensing.sdk.generator.ClientIdGenerator;
import com.c9.licensing.sdk.model.LicenseStatus;
import com.c9.licensing.sdk.model.LicenseValidationRequest;
import com.c9.licensing.sdk.model.LicenseValidationResponse;
import com.c9.licensing.sdk.model.server.LicenseServerServiceStatus;
import com.c9.licensing.sdk.model.server.LicenseServerValidationRequest;
import com.c9.licensing.sdk.model.server.LicenseServerValidationRequest.Builder;
import com.c9.licensing.sdk.model.server.LicenseServerValidationResponse;
import com.c9.licensing.sdk.service.LicenseOrchestrationService;
import com.c9.licensing.sdk.service.LicenseService;
import com.c9.licensing.sdk.service.LicenseTokenService;

@Service
public class LicenseOrchestrationServiceImpl implements LicenseOrchestrationService {

	private final LicenseService licenseService;
	private final LicenseTokenService licenseTokenService;
	private final ClientIdGenerator clientIdGenerator;

	public LicenseOrchestrationServiceImpl(LicenseService licenseService, LicenseTokenService licenseTokenService,
			ClientIdGenerator clientIdGenerator) {
		this.licenseService = licenseService;
		this.licenseTokenService = licenseTokenService;
		this.clientIdGenerator = clientIdGenerator;
	}

	@Override
	public LicenseValidationResponse getLicenseDetails(LicenseValidationRequest request) {
		String clientId = clientIdGenerator.getClientId(request);

		LicenseServerValidationRequest serverRequest = createServerRequest(clientId, request);
		LicenseServerValidationResponse serverResponse = licenseService.getLicenseDetails(serverRequest);
		refreshLicenseToken(clientId, serverResponse);

		if (serverResponse != null) {
			String status = serverResponse.status();
			String message = serverResponse.message();
			String serverStatus = serverResponse.status();

			if (serverResponse.success() && (TOKEN_CREATED.name().equals(serverStatus)
					|| TOKEN_REFRESHED.name().equals(serverStatus) || TOKEN_ACTIVE.name().equals(serverStatus))) {
				status = LicenseStatus.LICENSE_ACTIVE.name();
				message = "License is active";
			}

			return new LicenseValidationResponse.Builder().success(serverResponse.success())
					.status(status)
					.message(message)
					.errorDetails(serverResponse.errorDetails())
					.build();
		}

		return new LicenseValidationResponse.Builder().success(false)
				.status(LicenseServerServiceStatus.UNKNOWN_ERROR.name())
				.message("Unknown Error")
				.build();
	}

	private LicenseServerValidationRequest createServerRequest(String clientId, LicenseValidationRequest request) {
		Builder serverRequestBuilder = new LicenseServerValidationRequest.Builder().serviceId(request.serviceId())
				.serviceVersion(request.serviceVersion())
				.instanceId(request.instanceId())
				.licenseKey(request.licenseKey())
				.checksum(request.checksum());

		String licenseToken = licenseTokenService.getLicenseToken(clientId);
		if (licenseToken != null) {
			serverRequestBuilder.licenseToken(licenseToken);
		}

		return serverRequestBuilder.build();
	}

	private void refreshLicenseToken(String clientId, LicenseServerValidationResponse serverResponse) {
		if (serverResponse != null) {
			String status = serverResponse.status();
			if (serverResponse.success()) {
				boolean isValidToken = TOKEN_CREATED.name().equals(status) || TOKEN_REFRESHED.name().equals(status);
				if (isValidToken) {
					licenseTokenService.storeLicenseToken(clientId, serverResponse.licenseToken());
				}
			}
		}
	}

}
