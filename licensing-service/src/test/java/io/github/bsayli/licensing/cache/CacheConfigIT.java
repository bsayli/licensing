package io.github.bsayli.licensing.cache;

import io.github.bsayli.licensing.LicensingServiceApplication;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.testconfig.EmbeddedRedisConfig;
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

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {LicensingServiceApplication.class, EmbeddedRedisConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("integration")
class CacheConfigIT {

    private static final String KEY_PREFIX = "it-key-";

    @Autowired
    CacheManager cacheManager;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    CacheProperties cacheProperties;

    @Test
    @DisplayName("Configured caches are registered and usable with Redis TTL applied")
    void cachesRegisteredAndWorkingWithTtl() {
        assertConfiguredCachesRegistered();

        for (Map.Entry<String, CacheProperties.CacheSpec> e : cacheProperties.caches().entrySet()) {
            String cacheName = e.getKey();
            CacheProperties.CacheSpec spec = e.getValue();

            Cache cache = cacheManager.getCache(cacheName);
            assertThat(cache).as("cache bean should exist: %s", cacheName).isNotNull();

            String key = KEY_PREFIX + cacheName;

            Optional<Object> sample = sampleValueFor(spec);
            if (sample.isPresent()) {
                cache.put(key, sample.get());
                Object readBack = cache.get(key, () -> null);
                assertThat(readBack).as("value should be readable from cache %s", cacheName).isNotNull();
            } else {
                cache.put(key, "ttl-probe");
            }

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

    private Optional<Object> sampleValueFor(CacheProperties.CacheSpec spec) {
        if (spec == null) return Optional.empty();

        String fqcn = spec.type();
        if (fqcn == null || fqcn.isBlank()) return Optional.of("it-value");

        try {
            if (fqcn.equals(LicenseInfo.class.getName())) {
                return Optional.of(sampleLicenseInfo());
            }
            if (fqcn.equals(ClientSessionSnapshot.class.getName())) {
                return Optional.of(sampleClientSessionSnapshot());
            }

            Class<?> type = Class.forName(fqcn);
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return Optional.of(ctor.newInstance());
        } catch (Throwable ignore) {
            return Optional.empty();
        }
    }

    private LicenseInfo sampleLicenseInfo() {
        LicenseStatus status = LicenseStatus.values()[0];
        return new LicenseInfo.Builder()
                .userId("it-" + UUID.randomUUID())
                .licenseTier("it-tier")
                .licenseStatus(status)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .maxCount(1)
                .remainingUsageCount(1)
                .build();
    }

    private Object sampleClientSessionSnapshot() {
        try {
            Constructor<?> noArgs = ClientSessionSnapshot.class.getDeclaredConstructor();
            noArgs.setAccessible(true);
            return noArgs.newInstance();
        } catch (Throwable ignore) {
        }

        try {
            Constructor<?>[] ctors = ClientSessionSnapshot.class.getDeclaredConstructors();
            Constructor<?> best = null;
            for (Constructor<?> c : ctors) {
                if (best == null || c.getParameterCount() < best.getParameterCount()) best = c;
            }
            if (best == null) throw new IllegalStateException("No constructor for ClientSessionSnapshot");
            best.setAccessible(true);

            Object[] args = new Object[best.getParameterCount()];
            Class<?>[] types = best.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                args[i] = defaultArg(types[i]);
            }
            return best.newInstance(args);
        } catch (Throwable ex) {
            throw new IllegalStateException("Cannot create sample ClientSessionSnapshot for cache round-trip", ex);
        }
    }

    private Object defaultArg(Class<?> t) {
        if (t == String.class) return "it-" + UUID.randomUUID();
        if (t == UUID.class) return UUID.randomUUID();
        if (t == boolean.class || t == Boolean.class) return false;
        if (t == int.class || t == Integer.class) return 0;
        if (t == long.class || t == Long.class) return 0L;
        if (t == double.class || t == Double.class) return 0d;
        if (t == float.class || t == Float.class) return 0f;
        if (t == short.class || t == Short.class) return (short) 0;
        if (t == byte.class || t == Byte.class) return (byte) 0;
        if (t.isEnum()) return t.getEnumConstants()[0];
        return null;
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