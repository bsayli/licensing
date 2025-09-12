// io.github.bsayli.licensing.sdk.config.CacheConfig
package io.github.bsayli.licensing.sdk.config;

import static io.github.bsayli.licensing.sdk.cache.CacheNames.CACHE_LICENSE_TOKENS;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean(name = CACHE_LICENSE_TOKENS)
  public Cache licenseTokens(@Value("${caching.spring.licenseTokenTTL:60m}") Duration ttl) {
    var nativeCache = Caffeine.newBuilder().maximumSize(5_000).expireAfterWrite(ttl).build();

    return new CaffeineCache(CACHE_LICENSE_TOKENS, nativeCache);
  }

  @Bean
  public CacheManager cacheManager(@Qualifier(CACHE_LICENSE_TOKENS) Cache licenseTokens) {
    var mgr = new SimpleCacheManager();
    mgr.setCaches(List.of(licenseTokens));
    return mgr;
  }
}
