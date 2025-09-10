package io.github.bsayli.licensing.sdk.api.controller;

import io.github.bsayli.licensing.sdk.model.LicenseValidationRequest;
import io.github.bsayli.licensing.sdk.model.LicenseValidationResponse;
import io.github.bsayli.licensing.sdk.service.LicenseOrchestrationService;
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
public class LicenseSdkController {

  private final LicenseOrchestrationService licenseService;

  public LicenseSdkController(LicenseOrchestrationService licenseService) {
    this.licenseService = licenseService;
  }

  @PostMapping("/validate")
  public ResponseEntity<LicenseValidationResponse> validateLicense(
      @NotNull(message = "'licenseKey' request param is required!")
          @Size(
              min = 200,
              max = 400,
              message = "License Key must be between {min} and {max} characters")
          @RequestParam
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
      @NotNull(message = "'X-Instance-ID' header param is required!")
          @Size(
              min = 20,
              max = 100,
              message = "Instance Id header param must be between {min} and {max} characters")
          @RequestHeader("X-Instance-ID")
          String instanceId,
      @Size(
              min = 20,
              max = 100,
              message = "Checksum header param must be between {min} and {max} characters")
          @RequestHeader(value = "X-Checksum", required = false)
          String checksum) {

    LicenseValidationRequest request =
        new LicenseValidationRequest.Builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVersion)
            .licenseKey(licenseKey)
            .instanceId(instanceId)
            .checksum(checksum)
            .build();

    LicenseValidationResponse response = licenseService.getLicenseDetails(request);
    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
  }
}
