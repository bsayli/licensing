package io.github.bsayli.licensing.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Value("${caching.spring.clientLicenseInfoTTL}")
  private Duration userInfoTtl;

  @Value("${caching.spring.clientLicenseInfoOffLineSupportTTL}")
  private Duration userOfflineTtl;

  @Value("${jwt.token.expiration}")
  private Duration jwtExpiration;

  @Bean
  CacheManager cacheManager() {
    var mgr = new CaffeineCacheManager();
    mgr.registerCustomCache(
        "userInfoCache",
        Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(userInfoTtl).build());

    mgr.registerCustomCache(
        "userOfflineInfoCache",
        Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(userOfflineTtl).build());

    Duration tokenRelatedTtl = jwtExpiration.multipliedBy(2);
    if (tokenRelatedTtl.compareTo(Duration.ofHours(3)) > 0) {
      tokenRelatedTtl = Duration.ofHours(3);
    }

    mgr.registerCustomCache(
        "activeClients",
        Caffeine.newBuilder().maximumSize(50_000).expireAfterWrite(tokenRelatedTtl).build());

    mgr.registerCustomCache(
        "blacklistedTokens",
        Caffeine.newBuilder().maximumSize(50_000).expireAfterWrite(tokenRelatedTtl).build());

    return mgr;
  }
}
