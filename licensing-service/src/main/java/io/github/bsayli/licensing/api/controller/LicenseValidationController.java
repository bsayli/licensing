package io.github.bsayli.licensing.api.controller;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.common.api.ApiResponse;
import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/licenses")
@Validated
public class LicenseValidationController {

  private final LicenseOrchestrationService service;
  private final LocalizedMessageResolver messageResolver;

  public LicenseValidationController(
      LicenseOrchestrationService service, LocalizedMessageResolver messageResolver) {
    this.service = service;
    this.messageResolver = messageResolver;
  }

  @PostMapping("/tokens")
  public ResponseEntity<ApiResponse<LicenseValidationResponse>> issueToken(
      @Valid @RequestBody IssueTokenRequest request) {
    var result = service.issueToken(request);
    String msg = messageResolver.getMessage("license.validation.success");
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(HttpStatus.OK, msg, result));
  }

  @PostMapping("/tokens/validate")
  public ResponseEntity<ApiResponse<LicenseValidationResponse>> validateToken(
      @RequestHeader("License-Token")
          @NotBlank(message = "{license.token.required}")
          @Size(min = 200, max = 400, message = "{license.token.size}")
          @Pattern(
              regexp = "^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$",
              message = "{license.token.format}")
          String licenseToken,
      @Valid @RequestBody ValidateTokenRequest request) {

    var result = service.validateToken(request, licenseToken);
    String msg = messageResolver.getMessage("license.validation.success");
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(HttpStatus.OK, msg, result));
  }
}
