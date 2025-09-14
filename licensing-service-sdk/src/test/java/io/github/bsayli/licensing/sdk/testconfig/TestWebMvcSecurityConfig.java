package io.github.bsayli.licensing.sdk.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestWebMvcSecurityConfig {

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  SecurityFilterChain testSecurity(HttpSecurity http) throws Exception {
    return http.securityMatcher("/v1/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(reg -> reg.anyRequest().permitAll())
        .build();
  }
}
