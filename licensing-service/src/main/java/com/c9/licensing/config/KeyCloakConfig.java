package com.c9.licensing.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import jakarta.ws.rs.client.ClientBuilder;

@Configuration
public class KeyCloakConfig {

	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.resource}")
	private String clientId;

	@Value("${keycloak.credentials.secret}")
	private String clientSecret;
	
	Keycloak keycloak;

	@Bean
	Keycloak keycloak() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    	CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
    	cm.setMaxTotal(10); // Increase max total connection to 200
    	cm.setDefaultMaxPerRoute(5); // Increase default max connection per route to 20
    	ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(httpClient);

    	ResteasyClient client = ((ResteasyClientBuilder) ClientBuilder.newBuilder()).httpEngine(engine).build();
        keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(client)
                .build();
        
        return keycloak;
	}
	
	 @PreDestroy
     public void closeKeycloak() {
		 if(keycloak != null) {
			 keycloak.close();
		 }
     }
}
