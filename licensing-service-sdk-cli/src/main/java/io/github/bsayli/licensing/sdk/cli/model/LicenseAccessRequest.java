package io.github.bsayli.licensing.sdk.cli.model;

public record LicenseAccessRequest(
    String licenseKey,
    String instanceId,
    String checksum,
    String serviceId,
    String serviceVersion) {}
