package io.github.bsayli.licensing.client.common.core;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class ApiClientExecutor {

  private final ResponseParser responseParser;

  public ApiClientExecutor(ResponseParser responseParser) {
    this.responseParser = responseParser;
  }

  public <T> ApiClientResponse<T> handle(
      String operation, Class<T> clazz, Supplier<ApiClientResponse<T>> supplier) {
    try {
      return supplier.get();
    } catch (HttpStatusCodeException e) {
      return responseParser.parseOrThrow(operation, e, clazz);
    }
  }
}
