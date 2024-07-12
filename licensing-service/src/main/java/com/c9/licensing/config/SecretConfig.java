package com.c9.licensing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.c9.licensing.security.EncryptionUtil;
import com.c9.licensing.security.JwtUtil;
import com.c9.licensing.security.UserIdUtil;
import com.c9.licensing.security.impl.EncryptionUtilImpl;
import com.c9.licensing.security.impl.JwtUtilImpl;
import com.c9.licensing.security.impl.UserIdUtilImpl;

@Configuration
public class SecretConfig {

    @Value("${license.secret.key}")
    private String licenseSecretKey;
    
    @Value("${userid.secret.key}")
    private String userIdSecretKey;
    
    @Value("${license.jwt.secret.key}")
    private String licenseJwtSecretKey;
    
    @Value("${jwt.token.expiration}")
    private Integer tokenExpirationInMinute;
    
    @Bean
    UserIdUtil userIdUti() {
		return new UserIdUtilImpl(userIdSecretKey);
	}
    
    @Bean
    EncryptionUtil encryptionUtil() {
		return new EncryptionUtilImpl(licenseSecretKey);
	}
    
    @Bean
    JwtUtil jwtUtil() {
    	return new JwtUtilImpl(licenseJwtSecretKey, tokenExpirationInMinute);
    }
   

}
