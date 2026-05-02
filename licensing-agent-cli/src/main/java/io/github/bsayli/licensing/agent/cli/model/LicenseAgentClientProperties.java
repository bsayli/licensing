package io.github.bsayli.licensing.agent.cli.model;

public record LicenseAgentClientProperties(
    String baseUrl,
    String appUser,
    String appPass,
    String apiPath,
    int connectTimeoutSeconds,
    int responseTimeoutSeconds,
    int retries,
    int retryIntervalSeconds) {}
