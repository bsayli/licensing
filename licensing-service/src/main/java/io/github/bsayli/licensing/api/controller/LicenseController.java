package io.github.bsayli.licensing.api.controller;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.api.validation.annotations.ValidLicenseToken;
import io.github.bsayli.licensing.common.api.ApiResponse;
import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/licenses")
@Validated
public class LicenseController {

  private final LicenseOrchestrationService service;
  private final LocalizedMessageResolver messageResolver;

  public LicenseController(
      LicenseOrchestrationService service, LocalizedMessageResolver messageResolver) {
    this.service = service;
    this.messageResolver = messageResolver;
  }

  @PostMapping("/access")
  public ResponseEntity<ApiResponse<LicenseAccessResponse>> createAccess(
      @Valid @RequestBody IssueAccessRequest request) {
    var result = service.issueAccess(request);
    String msg = messageResolver.getMessage("license.validation.success");
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(HttpStatus.OK, msg, result));
  }

  @PostMapping("/access/validate")
  public ResponseEntity<ApiResponse<LicenseAccessResponse>> validateAccess(
      @RequestHeader("License-Token") @ValidLicenseToken String licenseToken,
      @Valid @RequestBody ValidateAccessRequest request) {

    var result = service.validateAccess(request, licenseToken);
    String msg = messageResolver.getMessage("license.validation.success");
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(HttpStatus.OK, msg, result));
  }
}
