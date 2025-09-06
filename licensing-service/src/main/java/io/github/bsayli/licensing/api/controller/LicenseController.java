package io.github.bsayli.licensing.api.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import io.github.bsayli.licensing.api.dto.ApiResponse;
import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.model.LicenseServiceStatus;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/licenses")
public class LicenseController {

  private final LicenseOrchestrationService service;

  public LicenseController(LicenseOrchestrationService service) {
    this.service = service;
  }

  @PostMapping("/validate")
  public ResponseEntity<ApiResponse<LicenseValidationResponse>> validate(
      @Valid @RequestBody LicenseValidationRequest request) {
    var result = service.getLicenseDetails(request);

    if (result.success()) {
      return ResponseEntity.status(OK).body(ApiResponse.of(OK, "LICENSE_VALID", result));
    }

    var status =
        LicenseServiceStatus.UNKNOWN_ERROR.name().equals(result.status())
            ? INTERNAL_SERVER_ERROR
            : UNAUTHORIZED;

    return ResponseEntity.status(status).body(ApiResponse.error(status, result.message()));
  }
}
