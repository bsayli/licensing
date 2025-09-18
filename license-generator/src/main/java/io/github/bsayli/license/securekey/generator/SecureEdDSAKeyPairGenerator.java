package io.github.bsayli.license.securekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.BC_PROVIDER;
import static io.github.bsayli.license.common.CryptoConstants.ED25519_STD_ALGO;
import static io.github.bsayli.license.common.CryptoConstants.RNG;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class SecureEdDSAKeyPairGenerator {

  private SecureEdDSAKeyPairGenerator() {}

  public static KeyPair generateKeyPair() throws GeneralSecurityException {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(ED25519_STD_ALGO);
      return kpg.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      ensureBouncyCastle();
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(ED25519_STD_ALGO, BC_PROVIDER);
      kpg.initialize(new ECGenParameterSpec(ED25519_STD_ALGO), RNG);
      return kpg.generateKeyPair();
    }
  }

  private static void ensureBouncyCastle() {
    if (Security.getProvider(BC_PROVIDER) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
}
