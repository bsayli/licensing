package com.c9.licensing.controller;

import com.c9.licensing.model.LicenseServiceStatus;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.response.LicenseValidationResponse;
import com.c9.licensing.service.LicenseOrchestrationService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
      @Size(
              min = 200,
              max = 400,
              message = "License Key must be between {min} and {max} characters")
          @RequestParam(required = false)
          String licenseKey,
      @NotNull(message = "'serviceId' request param is required!")
          @Size(
              min = 5,
              max = 50,
              message = "Service Id must be between {min} and {max} characters")
          @RequestParam
          String serviceId,
      @NotNull(message = "'serviceVersion' request param is required!")
          @Size(
              min = 5,
              max = 10,
              message = "Service Version must be between {min} and {max} characters")
          @RequestParam
          String serviceVersion,
      @Size(
              min = 200,
              max = 400,
              message = "License Token header param must be between {min} and {max} characters")
          @RequestHeader(value = "X-License-Token", required = false)
          String licenseToken,
      @NotNull(message = "'X-Instance-ID' header param is required!")
          @Size(
              min = 20,
              max = 100,
              message = "Instance Id header param must be between {min} and {max} characters")
          @RequestHeader("X-Instance-ID")
          String instanceId,
      @NotNull(message = "'Signature' header param is required!")
          @Size(
              min = 20,
              max = 100,
              message = "Signature header param must be between {min} and {max} characters")
          @RequestHeader(value = "X-Signature")
          String signature,
      @Size(
              min = 20,
              max = 100,
              message = "Checksum header param must be between {min} and {max} characters")
          @RequestHeader(value = "X-Checksum", required = false)
          String checksum,
      @RequestHeader(value = "X-Force-Token-Refresh", required = false) boolean forceTokenRefresh) {

    LicenseValidationRequest request =
        new LicenseValidationRequest.Builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVersion)
            .licenseKey(licenseKey)
            .licenseToken(licenseToken)
            .instanceId(instanceId)
            .checksum(checksum)
            .signature(signature)
            .forceTokenRefresh(forceTokenRefresh)
            .build();

    LicenseValidationResponse response = licenseOrchestrationService.getLicenseDetails(request);
    return getHttpResponse(response);
  }

  private ResponseEntity<LicenseValidationResponse> getHttpResponse(
      LicenseValidationResponse response) {
    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      if (LicenseServiceStatus.UNKNOWN_ERROR.name().equals(response.status())) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
  }
}
