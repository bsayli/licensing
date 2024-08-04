package com.c9.licensing.sdk.config;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class LicenseServerRestConfig {

	@Value("${licensing.server.base.url}")
	private String licensingServerBaseUrl;

	@Value("${licensing.server.app.user}")
	private String licensingServerAppUser;

	@Value("${licensing.server.app.pass}")
	private String licensingServerAppPass;


	@Bean
	RestClient licensingRestClient() {
		return RestClient.builder()
				.baseUrl(licensingServerBaseUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, createBasicAuthHeader(licensingServerAppUser, licensingServerAppPass))
				.build();
	}

    private String createBasicAuthHeader(String appUser, String appPass) {
        String authString = appUser + ":" + appPass;
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
        return "Basic " + new String(authEncBytes);
    }
}
