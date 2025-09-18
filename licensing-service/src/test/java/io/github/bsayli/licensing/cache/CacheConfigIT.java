package io.github.bsayli.licensing.cache;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bsayli.licensing.LicensingServiceApplication;
import io.github.bsayli.licensing.testconfig.EmbeddedRedisConfig;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {LicensingServiceApplication.class, EmbeddedRedisConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("integration")
class CacheConfigIT {

  private static final String KEY_PREFIX = "it-key-";

  @Autowired CacheManager cacheManager;
  @Autowired StringRedisTemplate stringRedisTemplate;
  @Autowired CacheProperties cacheProperties;

  @Test
  @DisplayName("Configured caches are registered and usable with Redis TTL applied")
  void cachesRegisteredAndWorkingWithTtl() {
    assertConfiguredCachesRegistered();

    for (Map.Entry<String, CacheProperties.CacheSpec> e : cacheProperties.caches().entrySet()) {
      final String cacheName = e.getKey();
      final CacheProperties.CacheSpec spec = e.getValue();

      Cache cache = cacheManager.getCache(cacheName);
      assertThat(cache).as("cache bean should exist: %s", cacheName).isNotNull();

      Optional<Object> sample = sampleInstance(spec.type());
      if (sample.isEmpty()) {
        Assumptions.assumeThat(false)
            .as(
                "skip round-trip for cache %s (no suitable no-args type: %s)",
                cacheName, spec.type())
            .isTrue();
        continue;
      }

      String key = KEY_PREFIX + cacheName;
      cache.put(key, sample.get());

      Object readBack = cache.get(key, () -> null);
      assertThat(readBack).as("value should be readable from cache %s", cacheName).isNotNull();

      String redisKey = cacheName + "::" + key;

      Long ttlSeconds = stringRedisTemplate.getExpire(redisKey);
      assertThat(ttlSeconds)
          .as("TTL should be set for key %s (cache %s)", redisKey, cacheName)
          .isNotNull()
          .isGreaterThan(0);

      long expectedUpperBound = expectedTtlSeconds(spec);
      if (expectedUpperBound > 0) {
        assertThat(ttlSeconds)
            .as("TTL should not exceed configured TTL for cache %s", cacheName)
            .isLessThanOrEqualTo(expectedUpperBound);
      }
    }
  }

  private void assertConfiguredCachesRegistered() {
    var expectedNames = cacheProperties.caches().keySet();
    assertThat(expectedNames).isNotEmpty();
    assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrderElementsOf(expectedNames);
  }

  private Optional<Object> sampleInstance(String fqcn) {
    try {
      if (fqcn == null || fqcn.isBlank()) {
        return Optional.of(new Object());
      }
      Class<?> type = Class.forName(fqcn);
      var ctor = type.getDeclaredConstructor();
      ctor.setAccessible(true);
      return Optional.of(ctor.newInstance());
    } catch (Throwable ignore) {
      return Optional.empty();
    }
  }

  private long expectedTtlSeconds(CacheProperties.CacheSpec spec) {
    Duration ttl = null;
    if (spec != null) {
      if (spec.ttlMinutes() != null) ttl = Duration.ofMinutes(spec.ttlMinutes());
      else if (spec.ttlHours() != null) ttl = Duration.ofHours(spec.ttlHours());
    }
    if (ttl == null && cacheProperties.defaultSpec() != null) {
      var def = cacheProperties.defaultSpec();
      if (def.ttlMinutes() != null) ttl = Duration.ofMinutes(def.ttlMinutes());
      else if (def.ttlHours() != null) ttl = Duration.ofHours(def.ttlHours());
    }
    return ttl == null ? -1 : ttl.getSeconds();
  }
}
