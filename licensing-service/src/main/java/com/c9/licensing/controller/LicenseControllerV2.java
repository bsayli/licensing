package com.c9.licensing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c9.licensing.model.LicenseErrorCode;
import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.response.LicenseValidationResponse;
import com.c9.licensing.security.JwtUtil;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.LicenseService;

import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/license/v2")
public class LicenseControllerV2 {

	private LicenseService licenseService;
	private JwtUtil jwtUtil;
	private LicenseClientCacheManagementService clientCacheManagementService;

	public LicenseControllerV2(LicenseService licenseService, 
			JwtUtil jwtUtil, 
			LicenseClientCacheManagementService clientCacheManagementService) {
		this.licenseService = licenseService;
		this.jwtUtil = jwtUtil;
		this.clientCacheManagementService = clientCacheManagementService;
	}

	@PostMapping("/validate")
	public ResponseEntity<LicenseValidationResponse> validateLicense(
			@RequestParam 
			@Size(min = 100, max = 300,  message = "License key must be between {min} and {max} characters") 
			String licenseKey,
			@RequestParam String serviceId,  
			@RequestHeader("X-App-Instance-ID") 
			@Size(min = 10, max = 100,  message = "Application Instance Id header param must be between {min} and {max} characters")
			String appInstanceId,
			@RequestHeader("X-Signature") String signature, 
			@RequestHeader("X-Checksum") String checksum) {

		LicenseValidationResult result = licenseService.getUserLicenseDetails(licenseKey, appInstanceId);
		if (result.valid()) {
			String token = jwtUtil.generateToken(result);
			clientCacheManagementService.addClientInfo(appInstanceId, token, result.userId());
			return ResponseEntity.ok(new LicenseValidationResponse.Builder().success(true).token(token)
					.message(result.message()).build());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LicenseValidationResponse.Builder()
					.success(false).errorCode(result.errorCode().name()).message(result.message()).build());
		}
	}

	@PostMapping("/validateToken")
	public ResponseEntity<LicenseValidationResponse> validateWithToken(
			@RequestHeader("X-License-Token") 
			@Size(min = 100, max = 300, message = "License Token header param must be between {min} and {max} characters") 
			String licenseToken,
			@RequestHeader("X-App-Instance-ID") 
			@Size(min = 10, max = 100,  message = "Application Instance Id header param must be between {min} and {max} characters")
			String appInstanceId) 
	{

		LicenseValidationResult result = licenseService.getUserLicenseDetailsByToken(licenseToken, appInstanceId);
		if (result.valid()) {
			if (LicenseErrorCode.TOKEN_REFRESHED == result.errorCode()) {
				String newToken = jwtUtil.generateToken(result);
				clientCacheManagementService.addClientInfo(appInstanceId, newToken, result.userId());
				return ResponseEntity.ok(new LicenseValidationResponse.Builder().success(true).token(newToken)
						.errorCode(result.errorCode().name()).message(result.message()).build());
			}
			return ResponseEntity.ok(new LicenseValidationResponse.Builder().success(true).token(licenseToken)
					.message(result.message()).build());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LicenseValidationResponse.Builder()
					.success(false).errorCode(result.errorCode().name()).message(result.message()).build());
		}

	}
}
