package com.c9.licensing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.response.LicenseValidationResponse;
import com.c9.licensing.service.LicenseOrchestrationService;

import jakarta.validation.constraints.NotNull;
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
			@NotNull(message = "'licenseKey' request param is required!")
			@Size(min = 200, max = 400, message = "License Key must be between {min} and {max} characters")
			@RequestParam String licenseKey,
			@NotNull(message = "'X-Instance-ID' header param is required!")
			@Size(min = 20, max = 100, message = "Instance Id header param must be between {min} and {max} characters")
			@RequestHeader("X-Instance-ID") String instanceId) {
		
		LicenseValidationRequest request = new LicenseValidationRequest.Builder()
				.licenseKey(licenseKey)
				.instanceId(instanceId)
				.build();

		LicenseValidationResponse response = licenseOrchestrationService.getLicenseDetailsByLicenseKey(request);
		if (response.success()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	@PostMapping("/validateToken")
	public ResponseEntity<LicenseValidationResponse> validateWithToken(
			@NotNull(message = "'X-License-Token' header param is required!")
			@Size(min = 200, max = 400, message = "License Token header param must be between {min} and {max} characters")
			@RequestHeader("X-License-Token") String licenseToken,
			@NotNull(message = "'X-Instance-ID' header param is required!")
			@Size(min = 20, max = 100, message = "Instance Id header param must be between {min} and {max} characters")
			@RequestHeader("X-Instance-ID") String instanceId) {

		LicenseValidationRequest request = new LicenseValidationRequest.Builder()
				.licenseToken(licenseToken)
				.instanceId(instanceId)
				.build();
		
		LicenseValidationResponse response = licenseOrchestrationService.getLicenseDetailsByToken(request);

		if (response.success()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

}
