package io.github.bsayli.licensing.client.adapter.config;

import io.github.bsayli.licensing.client.generated.api.LicenseValidationControllerApi;
import io.github.bsayli.licensing.client.generated.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class LicensingServiceApiClientConfig {

  @Bean
  public HttpComponentsClientHttpRequestFactory licensingServiceApiRequestFactory(
      @Value("${licensing-service-api.connect-timeout:10000}") int connectTimeoutMs,
      @Value("${licensing-service-api.connection-request-timeout:10000}")
          int connectionRequestTimeoutMs,
      @Value("${licensing-service-api.read-timeout:15000}") int readTimeoutMs) {
    var factory = new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeoutMs);
    factory.setConnectionRequestTimeout(connectionRequestTimeoutMs);
    factory.setReadTimeout(readTimeoutMs);
    return factory;
  }

  @Bean
  public RestClient licensingServiceApiRestClient(
      RestClient.Builder builder,
      HttpComponentsClientHttpRequestFactory licensingServiceApiRequestFactory,
      @Value("${licensing-service-api.base-url}") String baseUrl) {
    builder.requestFactory(licensingServiceApiRequestFactory);
    return builder.baseUrl(baseUrl).build();
  }

  @Bean
  public ApiClient licensingServiceApiClient(
      RestClient licensingServiceApiRestClient,
      @Value("${licensing-service-api.base-url}") String baseUrl) {
    return new ApiClient(licensingServiceApiRestClient).setBasePath(baseUrl);
  }

  @Bean
  public LicenseValidationControllerApi licenseValidationControllerApi(
      ApiClient licensingServiceApiClient) {
    return new LicenseValidationControllerApi(licensingServiceApiClient);
  }
}
