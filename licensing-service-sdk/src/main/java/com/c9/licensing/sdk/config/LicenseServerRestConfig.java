package com.c9.licensing.sdk.config;

import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class LicenseServerRestConfig {

  @Value("${LICENSE_SERVICE_URL:${licensing.server.url}}")
  private String licensingServerUrl;

  @Value("${licensing.server.app.user}")
  private String licensingServerAppUser;

  @Value("${licensing.server.app.pass}")
  private String licensingServerAppPass;

  @Bean
  RestClient licensingRestClient() {
    ClientHttpRequestFactory clientHttpRequestFactory = getClientHttpRequestFactory();
    return RestClient.builder()
        .baseUrl(licensingServerUrl)
        .requestFactory(clientHttpRequestFactory)
        .defaultHeader(
            HttpHeaders.AUTHORIZATION,
            createBasicAuthHeader(licensingServerAppUser, licensingServerAppPass))
        .build();
  }

  private ClientHttpRequestFactory getClientHttpRequestFactory() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    ConnectionConfig connectionConfig =
        ConnectionConfig.custom()
            .setConnectTimeout(Timeout.of(35, TimeUnit.SECONDS))
            .setSocketTimeout(Timeout.of(35, TimeUnit.SECONDS))
            .build();

    connectionManager.setDefaultConnectionConfig(connectionConfig);
    connectionManager.setMaxTotal(20);
    connectionManager.setDefaultMaxPerRoute(5);

    RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.of(10, TimeUnit.SECONDS))
            .setResponseTimeout(Timeout.of(35, TimeUnit.SECONDS))
            .build();

    CloseableHttpClient httpClient =
        HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

    return new HttpComponentsClientHttpRequestFactory(httpClient);
  }

  private String createBasicAuthHeader(String appUser, String appPass) {
    String authString = appUser + ":" + appPass;
    byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
    return "Basic " + new String(authEncBytes);
  }
}
