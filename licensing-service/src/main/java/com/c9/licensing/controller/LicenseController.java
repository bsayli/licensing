package com.c9.licensing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.c9.licensing.model.LicenseErrorCode;
import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.response.LicenseValidationResponse;
import com.c9.licensing.security.impl.JwtUtil;
import com.c9.licensing.service.LicenseService;
import com.c9.licensing.service.TokenCacheService;

@RestController
@RequestMapping("/api/license")
public class LicenseController {

	@Autowired
	private LicenseService licenseService;

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private TokenCacheService tokenCacheService;

	@PostMapping("/validate")
	public ResponseEntity<LicenseValidationResponse> validateLicense(@RequestParam String licenseKey,
			@RequestHeader("X-App-Instance-ID") String appInstanceId) {
		
		LicenseValidationResult result = licenseService.getLicenseResult(licenseKey, appInstanceId);
		if (result.valid()) {
			String token = jwtUtil.generateToken(result);
			tokenCacheService.addValidToken(token);
			return ResponseEntity.ok(new LicenseValidationResponse.Builder()
				    .success(true)
				    .token(token)
				    .message(result.message())
				    .build()); 
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new LicenseValidationResponse.Builder()
				    .success(false)
				    .errorCode(result.errorCode().name())
				    .message(result.message())
				    .build());
		}
	}

	@PostMapping("/validateToken")
	public ResponseEntity<LicenseValidationResponse> validateWithToken(@RequestHeader("X-License-Token") String licenseToken,
			@RequestHeader("X-App-Instance-ID") String appInstanceId) {
	
		LicenseValidationResult result = licenseService.validateAndRefreshToken(licenseToken, appInstanceId);
		if (result.valid()) {
			if(LicenseErrorCode.TOKEN_REFRESHED == result.errorCode()) {
				String newToken = jwtUtil.generateToken(result);
				tokenCacheService.addValidToken(newToken);
				return ResponseEntity.ok(new LicenseValidationResponse.Builder()
					    .success(true)
					    .token(newToken)
					    .errorCode(result.errorCode().name())
					    .message(result.message())
					    .build()); 
			}
			return ResponseEntity.ok(new LicenseValidationResponse.Builder()
				    .success(true)
				    .token(licenseToken)
				    .message(result.message())
				    .build()); 
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new LicenseValidationResponse.Builder()
				    .success(false)
				    .errorCode(result.errorCode().name())
				    .message(result.message())
				    .build());
		}
		
	}
}
