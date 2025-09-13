package io.github.bsayli.licensing.sdk.config;

import io.github.bsayli.licensing.sdk.generator.SignatureGenerator;
import io.github.bsayli.licensing.sdk.generator.impl.SignatureGeneratorImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecretConfig {

  @Value("${signature.private.key}")
  private String signaturePrivateKey;

  @Bean
  public SignatureGenerator signatureGenerator(
      @Value("${signature.private.key}") String privateKeyPkcs8Base64) {
    return new SignatureGeneratorImpl(privateKeyPkcs8Base64);
  }
}
