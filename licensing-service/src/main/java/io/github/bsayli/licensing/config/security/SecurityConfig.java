package io.github.bsayli.licensing.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(BasicAuthProperties.class)
public class SecurityConfig {

  private static final String[] WHITELIST = {
    "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs.yaml"
  };

  @Bean
  UserDetailsService userDetailsService(BasicAuthProperties props, PasswordEncoder encoder) {
    var user =
        User.withUsername(props.username())
            .password(encoder.encode(props.password()))
            .roles("USER")
            .build();
    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, RestAuthenticationEntryPoint entryPoint) throws Exception {
    http.csrf(CsrfConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(WHITELIST)
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(basic -> basic.authenticationEntryPoint(entryPoint))
        .headers(Customizer.withDefaults());
    return http.build();
  }
}
