package io.github.bsayli.licensing.config;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "license.service")
public record ServiceCatalogProperties(Set<String> ids, Set<String> checksumRequired) {
  public ServiceCatalogProperties {
    ids = (ids == null) ? Set.of() : Set.copyOf(ids);
    checksumRequired = (checksumRequired == null) ? Set.of() : Set.copyOf(checksumRequired);
  }
}
