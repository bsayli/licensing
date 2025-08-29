package com.c9.licensing.sdk.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    PrintWriter writer = response.getWriter();
    writer.println(
        "{\"success\": false, \"status\": \"UNAUTHORIZED\", \"message\": \"Authentication failed\", \"errorDetails\": null}");
  }

  @Override
  public void afterPropertiesSet() {
    setRealmName("C9LicensingSdk");
    super.afterPropertiesSet();
  }
}
