package io.github.bsayli.licensing.config;

import io.github.bsayli.licensing.security.LicenseKeyEncryptor;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.security.impl.LicenseKeyEncryptorImpl;
import io.github.bsayli.licensing.security.impl.SignatureValidatorImpl;
import io.github.bsayli.licensing.security.impl.UserIdEncryptorImpl;
import io.github.bsayli.licensing.service.jwt.JwtService;
import io.github.bsayli.licensing.service.jwt.impl.JwtServiceImpl;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
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

  // YAML: jwt.token.expiration: 60m
  @Value("${jwt.token.expiration}")
  private Duration tokenTtl;

  // YAML: jwt.token.max.jitter: 2m
  @Value("${jwt.token.max.jitter}")
  private Duration tokenMaxJitter;

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
    return new JwtServiceImpl(licenseJwtPrivateKey, licenseJwtPublicKey, tokenTtl, tokenMaxJitter);
  }

  @Bean
  SignatureValidator signatureValidator() {
    return new SignatureValidatorImpl(signaturePublicKey);
  }
}
