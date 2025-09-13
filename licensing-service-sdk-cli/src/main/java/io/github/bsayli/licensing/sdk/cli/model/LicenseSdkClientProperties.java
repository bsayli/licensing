package io.github.bsayli.licensing.sdk.cli.model;

public record LicenseSdkClientProperties(
    String baseUrl,
    String appUser,
    String appPass,
    String apiPath,
    int connectTimeoutSeconds,
    int responseTimeoutSeconds,
    int retries,
    int retryIntervalSeconds) {}
