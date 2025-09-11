package io.github.bsayli.licensing.cache;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(CacheSpec defaultSpec, Map<String, CacheSpec> caches) {

  public record CacheSpec(Integer ttlMinutes, Integer ttlHours, String type) {}
}
