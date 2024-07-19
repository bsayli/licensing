package com.c9.licensing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c9.licensing.response.LicenseValidationResponse;
import com.c9.licensing.service.LicenseOrchestrationService;

import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/license")
public class LicenseController {

	private final LicenseOrchestrationService licenseOrchestrationService;

	public LicenseController(LicenseOrchestrationService licenseOrchestrationService) {
		this.licenseOrchestrationService = licenseOrchestrationService;
	}

	@PostMapping("/validate")
	public ResponseEntity<LicenseValidationResponse> validateLicense(
			@Size(min = 100, max = 300, message = "License Key must be between {min} and {max} characters")
			@RequestParam String licenseKey,
			@Size(min = 10, max = 100, message = "Application Instance Id header param must be between {min} and {max} characters")
			@RequestHeader("X-App-Instance-ID") String appInstanceId) {

		LicenseValidationResponse response = licenseOrchestrationService.getLicenseDetails(licenseKey, appInstanceId);
		if (response.success()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	@PostMapping("/validateToken")
	public ResponseEntity<LicenseValidationResponse> validateWithToken(
			@Size(min = 100, max = 300, message = "License Token header param must be between {min} and {max} characters")
			@RequestHeader("X-License-Token") String licenseToken,
			@Size(min = 10, max = 100, message = "Application Instance Id header param must be between {min} and {max} characters")
			@RequestHeader("X-App-Instance-ID") String appInstanceId) {

		LicenseValidationResponse response = licenseOrchestrationService.getLicenseDetailsByToken(licenseToken,
				appInstanceId);

		if (response.success()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

}
