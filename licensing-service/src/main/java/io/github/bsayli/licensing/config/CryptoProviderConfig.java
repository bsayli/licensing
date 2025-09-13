package io.github.bsayli.licensing.config;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoProviderConfig {

  @Bean(name = "bcProvider")
  public Provider bcProvider() {
    Provider existing = Security.getProvider("BC");
    if (existing == null) {
      existing = new BouncyCastleProvider();
      Security.addProvider(existing);
    }
    return existing;
  }

  @Bean
  public KeyFactory eddsaKeyFactory(@Qualifier("bcProvider") Provider bcProvider)
      throws NoSuchAlgorithmException {
    return KeyFactory.getInstance("EdDSA", bcProvider);
  }
}
