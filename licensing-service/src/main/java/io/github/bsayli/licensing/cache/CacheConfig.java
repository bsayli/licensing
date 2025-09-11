package io.github.bsayli.licensing.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

  private final CacheProperties cacheProperties;

  public CacheConfig(CacheProperties cacheProperties) {
    this.cacheProperties = cacheProperties;
  }

  @Bean
  public RedisCacheManager cacheManager(
      RedisConnectionFactory connectionFactory,
      @Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper) {

    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

    cacheProperties
        .caches()
        .forEach(
            (name, spec) -> {
              Jackson2JsonRedisSerializer<?> serializer = buildSerializer(spec, cacheObjectMapper);
              RedisCacheConfiguration config = buildRedisCacheConfiguration(spec, serializer);
              cacheConfigs.put(name, config);
            });

    Jackson2JsonRedisSerializer<?> defaultSerializer =
        buildSerializer(cacheProperties.defaultSpec(), cacheObjectMapper);

    RedisCacheConfiguration defaultConfig =
        buildRedisCacheConfiguration(cacheProperties.defaultSpec(), defaultSerializer);

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigs)
        .build();
  }

  private Jackson2JsonRedisSerializer<?> buildSerializer(
      CacheProperties.CacheSpec spec, ObjectMapper mapper) {

    Class<?> targetClass = Object.class;

    if (spec != null && spec.type() != null && !spec.type().isBlank()) {
      try {
        targetClass = Class.forName(spec.type());
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("Invalid cache type: " + spec.type(), e);
      }
    }

    JavaType javaType = mapper.getTypeFactory().constructType(targetClass);
    return new Jackson2JsonRedisSerializer<>(mapper, javaType);
  }

  private RedisCacheConfiguration buildRedisCacheConfiguration(
      CacheProperties.CacheSpec spec, Jackson2JsonRedisSerializer<?> valueSerializer) {

    Duration ttl = Duration.ofMinutes(10);
    if (spec != null) {
      if (spec.ttlMinutes() != null) {
        ttl = Duration.ofMinutes(spec.ttlMinutes());
      } else if (spec.ttlHours() != null) {
        ttl = Duration.ofHours(spec.ttlHours());
      }
    }

    return RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues()
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
        .entryTtl(ttl);
  }

  @Bean(name = "cacheObjectMapper")
  public ObjectMapper cacheObjectMapper() {
    return new ObjectMapper()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  @Bean
  public SimpleCacheErrorHandler cacheErrorHandler() {
    return new SimpleCacheErrorHandler();
  }
}
