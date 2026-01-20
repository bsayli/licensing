package io.github.bsayli.licensing.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(CacheSpec defaultSpec, Map<String, CacheSpec> caches) {

    public record CacheSpec(Integer ttlMinutes, Integer ttlHours, String type) {
    }
}
