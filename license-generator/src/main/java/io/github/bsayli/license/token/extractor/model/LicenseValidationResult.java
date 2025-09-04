package io.github.bsayli.license.token.extractor.model;

import java.util.Date;

public record LicenseValidationResult(
    String licenseStatus, String licenseTier, String message, Date expirationDate) {}
