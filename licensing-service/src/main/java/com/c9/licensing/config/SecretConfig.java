package com.c9.licensing.config;

import com.c9.licensing.security.LicenseKeyEncryptor;
import com.c9.licensing.security.SignatureValidator;
import com.c9.licensing.security.UserIdEncryptor;
import com.c9.licensing.security.impl.LicenseKeyEncryptorImpl;
import com.c9.licensing.security.impl.SignatureValidatorImpl;
import com.c9.licensing.security.impl.UserIdEncryptorImpl;
import com.c9.licensing.service.jwt.JwtService;
import com.c9.licensing.service.jwt.impl.JwtServiceImpl;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecretConfig {

  @Value("${license.secret.key}")
  private String licenseSecretKey;

  @Value("${userid.secret.key}")
  private String userIdSecretKey;

  @Value("${signature.public.key}")
  private String signaturePublicKey;

  @Value("${jwt.token.expiration}")
  private Integer tokenExpirationInMinute;

  @Value("${jwt.token.max.jitter}")
  private Long tokenMaxJitter;

  @Value("${license.jwt.private.key}")
  private String licenseJwtPrivateKey;

  @Value("${license.jwt.public.key}")
  private String licenseJwtPublicKey;

  @Bean
  UserIdEncryptor userIdEncryptor() {
    return new UserIdEncryptorImpl(userIdSecretKey);
  }

  @Bean
  LicenseKeyEncryptor licenseKeyEncryptor() {
    return new LicenseKeyEncryptorImpl(licenseSecretKey);
  }

  @Bean
  JwtService jwtService()
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    return new JwtServiceImpl(
        licenseJwtPrivateKey, licenseJwtPublicKey, tokenExpirationInMinute, tokenMaxJitter);
  }

  @Bean
  SignatureValidator signatureValidator() {
    return new SignatureValidatorImpl(signaturePublicKey);
  }
}
