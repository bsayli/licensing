package io.github.bsayli.licensing.testconfig;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Configuration
public class EmbeddedRedisConfig {

  private final RedisProperties redisProperties;
  private RedisServer redisServer;

  public EmbeddedRedisConfig(RedisProperties redisProperties) {
    this.redisProperties = redisProperties;
  }

  @PostConstruct
  public void startRedis() throws IOException {
    if (isPortAvailable(redisProperties.getRedisPort())) {
      redisServer = new RedisServer(redisProperties.getRedisPort());
      redisServer.start();
    } else {
      System.err.printf(
          "Port %d is already in use. Skipping Redis start.%n", redisProperties.getRedisPort());
    }
  }

  @PreDestroy
  public void stopRedis() {
    if (redisServer != null && redisServer.isActive()) {
      try {
        redisServer.stop();
      } catch (Exception e) {
        System.err.printf("Embedded Redis shutdown failed: %s%n", e.getMessage());
      } finally {
        redisServer = null;
      }
    }
  }

  private boolean isPortAvailable(int port) {
    try (ServerSocket ignored = new ServerSocket(port)) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
