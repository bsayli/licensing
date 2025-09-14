package io.github.bsayli.licensing.sdk.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestRestClientConfig {

  @Bean
  @Primary
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }
}
