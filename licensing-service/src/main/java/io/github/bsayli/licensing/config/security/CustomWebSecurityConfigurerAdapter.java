package io.github.bsayli.licensing.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class CustomWebSecurityConfigurerAdapter {

  @Value("${app.user}")
  private String appUser;

  @Value("${app.pass}")
  private String appPass;

  @Autowired private RestAuthenticationEntryPoint authenticationEntryPoint;

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder)
      throws Exception {
    auth.inMemoryAuthentication()
        .withUser(appUser)
        .password(passwordEncoder.encode(appPass))
        .authorities("ROLE_USER");
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            expressionInterceptUrlRegistry ->
                expressionInterceptUrlRegistry.anyRequest().authenticated())
        .httpBasic(
            httpSecurityHttpBasicConfigurer ->
                httpSecurityHttpBasicConfigurer.authenticationEntryPoint(authenticationEntryPoint));
    http.csrf(CsrfConfigurer::disable);
    http.sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class);
    return http.build();
  }
}
