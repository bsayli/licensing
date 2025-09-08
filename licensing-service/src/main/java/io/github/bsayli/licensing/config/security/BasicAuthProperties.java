package io.github.bsayli.licensing.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "licensing.api.basic")
public record BasicAuthProperties(String username, String password, String realm) {}
