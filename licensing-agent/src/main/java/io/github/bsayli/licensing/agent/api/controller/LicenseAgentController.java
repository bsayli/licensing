package io.github.bsayli.licensing.agent.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.service.LicenseOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/licenses")
@Validated
public class LicenseAgentController {

    private final LicenseOrchestrationService licenseService;

  public LicenseAgentController(LicenseOrchestrationService licenseService) {
        this.licenseService = licenseService;
    }

    @PostMapping("/access")
    public ResponseEntity<ServiceResponse<LicenseToken>> getLicenseToken(
            @Valid @RequestBody LicenseAccessRequest request) {

        LicenseToken token = licenseService.getLicenseToken(request);
        return ResponseEntity.status(HttpStatus.OK).body(ServiceResponse.of(token));
    }
}
