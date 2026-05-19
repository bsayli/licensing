package io.github.bsayli.licensing.client.common.core;

import io.github.bsayli.licensing.contract.api.ApiResponse;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class ApiClientExecutor {

  private final ResponseParser responseParser;

  public ApiClientExecutor(ResponseParser responseParser) {
    this.responseParser = responseParser;
  }

  public <T> ApiResponse<T> handle(
      String operation, Class<T> clazz, Supplier<ApiResponse<T>> supplier) {
    try {
      return supplier.get();
    } catch (HttpStatusCodeException e) {
      return responseParser.parseOrThrow(operation, e, clazz);
    }
  }
}
