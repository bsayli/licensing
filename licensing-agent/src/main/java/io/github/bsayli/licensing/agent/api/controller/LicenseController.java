package io.github.bsayli.licensing.agent.api.controller;

import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.common.api.ApiResponse;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
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
