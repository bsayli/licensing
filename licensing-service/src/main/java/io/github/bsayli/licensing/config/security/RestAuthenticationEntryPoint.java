// src/main/java/io/github/bsayli/licensing/config/security/RestAuthenticationEntryPoint.java
package io.github.bsayli.licensing.config.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

  private final BasicAuthProperties props;

  public RestAuthenticationEntryPoint(BasicAuthProperties props) {
    this.props = props;
  }

  @PostConstruct
  public void init() {
    setRealmName(props.realm() != null ? props.realm() : "LicensingService");
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    String body =
        """
      {"success":false,"status":"UNAUTHORIZED","message":"Authentication failed","errorDetails":null}
      """;
    response.getWriter().println(body);
  }
}
