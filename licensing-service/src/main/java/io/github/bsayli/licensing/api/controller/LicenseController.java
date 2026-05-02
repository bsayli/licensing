package io.github.bsayli.licensing.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.api.validation.annotations.ValidLicenseToken;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/licenses")
@Validated
public class LicenseController {

    private final LicenseOrchestrationService service;

    public LicenseController(
            LicenseOrchestrationService service) {
        this.service = service;
    }

    @PostMapping("/access")
    public ResponseEntity<ServiceResponse<LicenseAccessResponse>> createAccess(
            @Valid @RequestBody IssueAccessRequest request) {
        var result = service.issueAccess(request);
        return ResponseEntity.ok(ServiceResponse.of(result));
    }

    @PostMapping("/access/validate")
    public ResponseEntity<ServiceResponse<LicenseAccessResponse>> validateAccess(
            @RequestHeader("License-Token") @ValidLicenseToken String licenseToken,
            @Valid @RequestBody ValidateAccessRequest request) {

        var result = service.validateAccess(request, licenseToken);
        return ResponseEntity.ok(ServiceResponse.of(result));
    }
}
