package io.github.bsayli.licensing.sdk.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "licensing.sdk.api.basic")
public record BasicAuthProperties(String username, String password, String realm) {}
