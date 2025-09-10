package io.github.bsayli.licensing.client.common.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.common.exception.ApiClientException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class ResponseParser {

  private final ObjectMapper objectMapper;

  public ResponseParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> ApiClientResponse<T> parseOrThrow(
      String operation, HttpStatusCodeException e, Class<T> targetClass) {

    String responseBody = e.getResponseBodyAsString();
    String contentType = getContentType(e);
    HttpStatusCode status = e.getStatusCode();

    if (!isJsonContent(contentType) || isLikelyHtml(responseBody)) {
      throw new ApiClientException(
          "["
              + operation
              + "] Non-JSON response from licensing-service. Content-Type: "
              + contentType,
          status,
          responseBody,
          "system.invalid.response");
    }

    try {
      return objectMapper.readValue(
          responseBody,
          objectMapper
              .getTypeFactory()
              .constructParametricType(ApiClientResponse.class, targetClass));
    } catch (Exception ex) {
      throw new ApiClientException(
          "[" + operation + "] Failed to parse JSON error response",
          status,
          responseBody,
          "system.unexpected.response",
          ex);
    }
  }

  private boolean isJsonContent(String contentType) {
    try {
      return contentType != null
          && MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(contentType));
    } catch (InvalidMediaTypeException ex) {
      return false;
    }
  }

  private boolean isLikelyHtml(String body) {
    return body != null && body.trim().startsWith("<");
  }

  private String getContentType(HttpStatusCodeException e) {
    return Optional.ofNullable(e.getResponseHeaders())
        .map(HttpHeaders::getContentType)
        .map(Object::toString)
        .orElse("");
  }
}
