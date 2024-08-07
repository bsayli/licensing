package com.c9.licensing.sdk.config;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.c9.licensing.sdk.generator.SignatureGenerator;
import com.c9.licensing.sdk.generator.impl.SignatureGeneratorImpl;

@Configuration
public class SecretConfig {

    @Value("${signature.private.key}")
    private String signaturePrivateKey;
    
    @Bean
    SignatureGenerator signatureValidator() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
    	return new SignatureGeneratorImpl(signaturePrivateKey);
    }
   
}
