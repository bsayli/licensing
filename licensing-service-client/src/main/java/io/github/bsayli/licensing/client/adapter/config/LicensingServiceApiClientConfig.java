package io.github.bsayli.licensing.client.adapter.config;

import io.github.bsayli.licensing.client.generated.api.LicenseControllerApi;
import io.github.bsayli.licensing.client.generated.invoker.ApiClient;
import java.time.Duration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class LicensingServiceApiClientConfig {

  @Bean(destroyMethod = "close")
  public CloseableHttpClient licensingServiceHttpClient(
      @Value("${licensing-service-api.max-connections-total:64}") int maxConnTotal,
      @Value("${licensing-service-api.max-connections-per-route:16}") int maxConnPerRoute) {

    var cm =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(maxConnTotal)
            .setMaxConnPerRoute(maxConnPerRoute)
            .build();

    return HttpClients.custom()
        .setConnectionManager(cm)
        .evictExpiredConnections()
        .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(30))
        .setUserAgent("licensing-service-client")
        .disableAutomaticRetries()
        .build();
  }

  @Bean
  public HttpComponentsClientHttpRequestFactory licensingServiceApiRequestFactory(
      CloseableHttpClient licensingServiceHttpClient,
      @Value("${licensing-service-api.connect-timeout-seconds:10}") long connectTimeoutSec,
      @Value("${licensing-service-api.connection-request-timeout-seconds:10}")
          long connectionReqTimeoutSec,
      @Value("${licensing-service-api.read-timeout-seconds:15}") long readTimeoutSec) {

    var factory = new HttpComponentsClientHttpRequestFactory(licensingServiceHttpClient);
    factory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSec));
    factory.setConnectionRequestTimeout(Duration.ofSeconds(connectionReqTimeoutSec));
    factory.setReadTimeout(Duration.ofSeconds(readTimeoutSec));
    return factory;
  }

  @Bean
  public RestClient licensingServiceApiRestClient(
      RestClient.Builder builder, HttpComponentsClientHttpRequestFactory requestFactory) {
    builder.requestFactory(requestFactory);
    return builder.build();
  }

  @Bean
  public ApiClient licensingServiceApiClient(
      RestClient licensingServiceApiRestClient,
      @Value("${licensing-service-api.base-url}") String baseUrl,
      @Value("${licensing-service-api.basic.username}") String username,
      @Value("${licensing-service-api.basic.password}") String password) {
    ApiClient client = new ApiClient(licensingServiceApiRestClient).setBasePath(baseUrl);
    client.setPassword(password);
    client.setUsername(username);
    return client;
  }

  @Bean
  public LicenseControllerApi licenseControllerApi(ApiClient licensingServiceApiClient) {
    return new LicenseControllerApi(licensingServiceApiClient);
  }
}
