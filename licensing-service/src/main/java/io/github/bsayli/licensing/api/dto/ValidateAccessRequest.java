package io.github.bsayli.licensing.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ValidateAccessRequest(
    @NotBlank(message = "{instance.id.required}")
        @Size(min = 10, max = 100, message = "{instance.id.size}")
        String instanceId,
    @Size(min = 40, max = 200, message = "{checksum.size}") String checksum,
    @NotBlank(message = "{service.id.required}")
        @Size(min = 3, max = 50, message = "{service.id.size}")
        String serviceId,
    @NotBlank(message = "{service.version.required}")
        @Size(min = 3, max = 20, message = "{service.version.size}")
        String serviceVersion,
    @NotBlank(message = "{signature.required}")
        @Size(min = 60, max = 200, message = "{signature.size}")
        String signature) {}
