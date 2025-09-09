package io.github.bsayli.licensing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {

  @Bean("retryProperties")
  public RetryProperties retryPropertiesBean(RetryProperties props) {
    return props;
  }
}
