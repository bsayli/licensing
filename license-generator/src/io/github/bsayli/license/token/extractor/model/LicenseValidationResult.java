package io.github.bsayli.license.token.extractor.model;

import java.util.Date;

/** Minimal projection of the license-related fields extracted from a valid JWT. */
public record LicenseValidationResult(
    String licenseStatus, String licenseTier, String message, Date expirationDate) {}
