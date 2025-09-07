package io.github.bsayli.licensing.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueTokenRequest(
    @NotBlank(message = "{service.id.required}")
        @Size(min = 3, max = 50, message = "{service.id.size}")
        String serviceId,
    @NotBlank(message = "{service.version.required}")
        @Size(min = 3, max = 20, message = "{service.version.size}")
        String serviceVersion,
    @NotBlank(message = "{instance.id.required}")
        @Size(min = 8, max = 200, message = "{instance.id.size}")
        String instanceId,
    @NotBlank(message = "{signature.required}")
        @Size(min = 20, max = 500, message = "{signature.size}")
        String signature,
    @Size(min = 20, max = 500, message = "{checksum.size}") String checksum,
    @NotBlank(message = "{license.key.required}")
        @Size(min = 200, max = 400, message = "{license.key.size}")
        String licenseKey,
    boolean forceTokenRefresh) {}
