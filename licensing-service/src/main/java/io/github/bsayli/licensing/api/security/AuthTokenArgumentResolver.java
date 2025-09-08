package io.github.bsayli.licensing.api.security;

import io.github.bsayli.licensing.service.exception.security.InvalidAuthorizationHeaderException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;

@Component
public class AuthTokenArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AuthToken.class)
        && parameter.getParameterType().equals(String.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mav,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    AuthToken ann = parameter.getParameterAnnotation(AuthToken.class);
    String authorization = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
    String token = extractBearer(authorization);

    if (ann != null && ann.required() && token == null) {
      throw new InvalidAuthorizationHeaderException();
    }
    return token;
  }

  private String extractBearer(@Nullable String authorization) {
    if (authorization == null || authorization.isBlank()) return null;
    int space = authorization.indexOf(' ');
    if (space <= 0) throw new InvalidAuthorizationHeaderException();
    String scheme = authorization.substring(0, space);
    String token = authorization.substring(space + 1).trim();
    if (!"Bearer".equalsIgnoreCase(scheme) || token.isEmpty()) {
      throw new InvalidAuthorizationHeaderException();
    }
    return token;
  }
}
