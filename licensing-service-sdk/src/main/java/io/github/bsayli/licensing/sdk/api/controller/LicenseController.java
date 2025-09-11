package io.github.bsayli.licensing.sdk.api.controller;

import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseToken;
import io.github.bsayli.licensing.sdk.common.api.ApiResponse;
import io.github.bsayli.licensing.sdk.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.sdk.service.LicenseOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/licenses")
@Validated
public class LicenseController {

  private final LicenseOrchestrationService licenseService;
  private final LocalizedMessageResolver messageResolver;

  public LicenseController(
      LicenseOrchestrationService licenseService, LocalizedMessageResolver messageResolver) {
    this.licenseService = licenseService;
    this.messageResolver = messageResolver;
  }

  @PostMapping("/access")
  public ResponseEntity<ApiResponse<LicenseToken>> getLicenseToken(
      @Valid @RequestBody LicenseAccessRequest request) {

    LicenseToken token = licenseService.getLicenseToken(request);

    String msg = messageResolver.getMessage("license.validation.success");
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(HttpStatus.OK, msg, token));
  }
}
