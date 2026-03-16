package io.github.bsayli.licensing.client.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.client.adapter.support.ProblemDetailSupport;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import io.github.bsayli.licensing.client.generated.api.LicenseControllerApi;
import io.github.bsayli.licensing.client.generated.invoker.ApiClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class LicensingServiceApiClientConfig {

    @Bean
    RestClientCustomizer problemDetailStatusHandler(ObjectMapper om) {
        return builder ->
                builder.defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            ProblemDetail pd = ProblemDetailSupport.extract(om, response);
                            throw new ApiProblemException(pd, response.getStatusCode().value());
                        });
    }

    @Bean(destroyMethod = "close")
    CloseableHttpClient licensingServiceHttpClient(
            @Value("${licensing-service-api.max-connections-total:64}") int maxTotal,
            @Value("${licensing-service-api.max-connections-per-route:16}") int maxPerRoute,
            @Value("${licensing-service-api.connect-timeout-seconds:10}") long connect,
            @Value("${licensing-service-api.connection-request-timeout-seconds:10}") long connReq,
            @Value("${licensing-service-api.read-timeout-seconds:15}") long read) {

        var connectionConfig =
                ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(connect)).build();

        var requestConfig =
                RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(connReq))
                        .setResponseTimeout(Timeout.ofSeconds(read))
                        .build();

        var cm =
                PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(maxTotal)
                        .setMaxConnPerRoute(maxPerRoute)
                        .setDefaultConnectionConfig(connectionConfig)
                        .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .setUserAgent("licensing-service-client")
                .disableAutomaticRetries()
                .build();
    }

    @Bean
    HttpComponentsClientHttpRequestFactory licensingServiceRequestFactory(
            CloseableHttpClient licensingServiceHttpClient) {
        return new HttpComponentsClientHttpRequestFactory(licensingServiceHttpClient);
    }

    @Bean
    RestClient licensingServiceRestClient(
            RestClient.Builder builder,
            HttpComponentsClientHttpRequestFactory licensingServiceRequestFactory,
            List<RestClientCustomizer> customizers) {
        builder.requestFactory(licensingServiceRequestFactory);
        if (customizers != null) {
            customizers.forEach(c -> c.customize(builder));
        }
        return builder.build();
    }

    @Bean
    ApiClient licensingServiceApiClient(
            RestClient licensingServiceRestClient,
            @Value("${licensing-service-api.base-url}") String baseUrl,
            @Value("${licensing-service-api.basic.username}") String username,
            @Value("${licensing-service-api.basic.password}") String password) {
        ApiClient client = new ApiClient(licensingServiceRestClient).setBasePath(baseUrl);
        client.setPassword(password);
        client.setUsername(username);
        return client;
    }

    @Bean
    LicenseControllerApi licenseControllerApi(ApiClient licensingServiceApiClient) {
        return new LicenseControllerApi(licensingServiceApiClient);
    }
}